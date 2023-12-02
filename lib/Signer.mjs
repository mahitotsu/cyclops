import { getSignedCookies } from '@aws-sdk/cloudfront-signer';

const keyPairId = process.env.CLOUDFRONT_KEY_PAIR_ID || 'K2KIYJ4DVC7OKV';
const privateKeySecretName = process.env.PRIVATE_KEY_SECRET_NAME || '/keypair/mahitotsu/private';
const cfDomain = process.env.CLOUD_FRONT_DOMAIN || 'd1ic6zuhadvpdn.cloudfront.net';
const secretExtensionPort = 2773;
const secretLocation = `http://localhost:${secretExtensionPort}/secretsmanager/get?secretId=${privateKeySecretName}`

export const handler = async (event, context) => {

    const options = [
        `Domain=${cfDomain}`, 'Path=/', 'Secure', 'HttpOnly', 'SameSite=Strict'
    ].join(';')

    const privateKey = (await fetch(secretLocation, {
        method: 'GET',
        headers: {
            'X-Aws-Parameters-Secrets-Token': process.env.AWS_SESSION_TOKEN,
        }
    }).then(res => res.json())).SecretString;
    const policy = {
        "Statement": [
            {
                "Resource": `https://${cfDomain}/*`,
                "Condition": {
                    "DateLessThan": {
                        "AWS:EpochTime": Math.floor((Date.now() + 24 * 60 * 60 * 1000) / 1000),
                    },
                }
            }
        ]
    };
    const cookieValues = getSignedCookies({
        keyPairId, privateKey,
        policy: JSON.stringify(policy),
    });

    return {
        statusCode: 303,
        headers: {
            'Location': '/',
        },
        cookies: [
            `CloudFront-Policy=${cookieValues['CloudFront-Policy']};${options}`,
            `CloudFront-Signature=${cookieValues['CloudFront-Signature']};${options}`,
            `CloudFront-Key-Pair-Id=${cookieValues['CloudFront-Key-Pair-Id']};${options}`,
        ],
        body: '',
    }
}