import { GetObjectCommand, S3Client, SelectObjectContentEventStreamFilterSensitiveLog } from '@aws-sdk/client-s3';
import { pipeline } from "node:stream/promises";
import { Readable } from 'node:stream';

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


export const handler = awslambda.streamifyResponse(async (event, responseStream) => {

    const rawPath = event.rawPath;

    await redirect_if_need(rawPath, responseStream);
    if (responseStream.writableEnded) {
        return;
    }

    const res = await new S3Client().send(new GetObjectCommand({
        Bucket: bucketName,
        Key: keyPrefix + get_object_key(rawPath),
    }));
    await response(200, { 'Content-Type': res.ContentType, }, res.Body, responseStream);
});
