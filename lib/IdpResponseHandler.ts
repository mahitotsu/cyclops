import { Handler } from "aws-lambda";
import * as AWS from 'aws-sdk';

const secretName = process.env.AUTH_SECRET_NAME!;
const secretsManager = new AWS.SecretsManager();
const secretString = (await (secretsManager.getSecretValue({ SecretId: secretName }).promise())).SecretString!;
const secretJson = JSON.parse(secretString);
const { userPoolClientId, userPoolClientSecret, distributionDomain, tokenEndpoint } = secretJson;
const basicAuth = Buffer.from(`${userPoolClientId}:${userPoolClientSecret}`).toString('base64');

export const handler: Handler = async (event, context) => {

    const headers = {
        'Authorization': `Basic ${basicAuth}`,
        'Content-Type': 'application/x-www-form-urlencoded',
    };

    const requestBody = {
        grant_type: 'authorization_code',
        client_id: userPoolClientId,
        client_secret: userPoolClientSecret,
        redirect_uri: `https://${distributionDomain}${event.rawPath}`,
        code: event.queryStringParameters.code,
    } as { [key: string]: string };
    const encodedForm = Object.keys(requestBody)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(requestBody[key])}`)
        .join('&');

    const { id_token, access_token, refresh_token, expires_in, token_type } = JSON.parse(
        await fetch(tokenEndpoint, {
            method: 'POST', headers, body: encodedForm,
        }).then(res => res.text())
    );
    console.log({ id_token, access_token, refresh_token, expires_in, token_type })

    return {
        statusCode: 302,
        headers: {
            'Location': '/',
        },
        body: '',
    }
}