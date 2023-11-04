import { Handler } from "aws-lambda";
import * as AWS from 'aws-sdk';
import * as cookie from 'cookie';

const secretName = process.env.AUTH_SECRET_NAME!;
const secretsManager = new AWS.SecretsManager();
const secretString = (await (secretsManager.getSecretValue({ SecretId: secretName }).promise())).SecretString!;
const secretJson = JSON.parse(secretString);
const { userPoolClientId, userPoolClientSecret, distributionDomain, tokenEndpoint } = secretJson;
const basicAuth = Buffer.from(`${userPoolClientId}:${userPoolClientSecret}`).toString('base64');

export const handler: Handler = async (event, context) => {

    // ----- 
    // get the authorization code sent from Cognito's hosted UI
    // -----
    const authCode = event.queryStringParameters ? event.queryStringParameters.code : undefined;
    if (authCode == undefined) {
        // the process end immediately because the required authorization code is not found
        return {
            statusCode: 404,
            body: '',
        };
    }

    // -----
    // get access/id/refresh tokens from cognito token endpoint
    // -----

    // configure headers
    const headers = {
        'Authorization': `Basic ${basicAuth}`,
        'Content-Type': 'application/x-www-form-urlencoded',
    };

    // configure request body
    const requestBody = {
        grant_type: 'authorization_code',
        client_id: userPoolClientId,
        client_secret: userPoolClientSecret,
        redirect_uri: `https://${distributionDomain}${event.rawPath}`,
        code: authCode,
    } as { [key: string]: string };
    const encodedForm = Object.keys(requestBody)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(requestBody[key])}`)
        .join('&');

    // send a request and parse the response
    const { id_token, access_token, refresh_token, expires_in, token_type } = JSON.parse(
        await fetch(tokenEndpoint, {
            method: 'POST', headers, body: encodedForm,
        }).then(res => res.text())
    );
    console.log({ id_token, access_token, refresh_token, expires_in, token_type })

    // -----
    // validate tokens
    // -----

    // -----
    // TODO

    // -----
    // issune http session key and store tokens with it
    // -----
    const currentTime = Date.now();
    const random = Math.random();
    const sessionKey = currentTime.toString() + random.toString();
    const sessionCookie = cookie.serialize('JSESSIONID', sessionKey, {
        domain: distributionDomain, path:'/', httpOnly: true, secure: true, maxAge: expires_in,
    });

    // ----
    // all processings are completed successfully, and redirect to the application entry point
    // ----
    return {
        statusCode: 302,
        headers: {
            'Location': `https://${distributionDomain}/`,
            'Set-Cookie': sessionCookie,
        },
        body: '',
    }
}