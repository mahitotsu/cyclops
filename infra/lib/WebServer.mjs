import { GetObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { Readable } from 'node:stream';
import { pipeline } from "node:stream/promises";

const bucketName = process.env.BUCKET_NAME;
const keyPrefix = process.env.KEY_PREFIX;

const response = async (statusCode, headers, body, responseStream) => {
    await pipeline(body, awslambda.HttpResponseStream.from(responseStream, { statusCode, headers, }));
}

const redirect_if_need = async (rawPath, responseStream) => {
    let location = undefined;
    if (rawPath.endsWith('.html')) {
        location = rawPath.substring(0, rawPath.length - '.html'.length);
    } else if (rawPath.endsWith('/index')) {
        location = rawPath.substring(0, rawPath.length - 'index'.length);
    }

    if (location != undefined) {
        await response(301, { 'Location': location }, Readable.from(['']), responseStream);
    }
}

const get_object_key = (rawPath) => {
    let objKey = rawPath;
    if (objKey.endsWith('/')) {
        objKey = objKey + 'index.html';
    } else if (objKey.lastIndexOf('.') < objKey.lastIndexOf('/')) {
        objKey = objKey + '/index.html';
    }
    return objKey;
}

const handle_auth_code = async (event, responseStream) => {
    const rawPath = event.rawPath;
    if (rawPath == '/oauth2/idpresponse') {
        await response(200, { 'Content-Type': 'text/plain' },
            Readable.from([JSON.stringify(event, null, 4)]), responseStream);
    }
}


export const handler = awslambda.streamifyResponse(async (event, responseStream) => {

    const rawPath = event.rawPath;

    await redirect_if_need(rawPath, responseStream);
    if (responseStream.writableEnded) {
        return;
    }

    await handle_auth_code(event, responseStream);
    if (responseStream.writableEnded) {
        return;
    }

    try {
        const res = await new S3Client().send(new GetObjectCommand({
            Bucket: bucketName,
            Key: keyPrefix + get_object_key(rawPath),
        }));
        await response(200, { 'Content-Type': res.ContentType, }, res.Body, responseStream);
    } catch (e) {
        if (e.code == 'NoSuchKey') {
            await response(404, { 'Content-Type': 'text/plain', },
                Readable.from(['Not found']), responseStream);
        } else {
            await response(500, { 'Content-Type': 'text/plain', },
                Readable.from(['Internal Server Error']), responseStream);
        }
    }
});
