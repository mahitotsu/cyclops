import { GetObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { pipeline } from "node:stream/promises";

const bucketName = process.env.BUCKET_NAME;
const keyPrefix = process.env.KEY_PREFIX;

export const handler = awslambda.streamifyResponse(async (event, responseStream) => {

    const rawPath = event.rawPath;
    let objKey = rawPath;
    if (objKey.endsWith('/')) {
        objKey = objKey + 'index.html';
    } else if (objKey.lastIndexOf('.') < objKey.lastIndexOf('/')) {
        objKey = objKey + '/index.html';
    }

    const client = new S3Client();
    const res = await client.send(new GetObjectCommand({
        Bucket: bucketName,
        Key: keyPrefix + objKey,
    }));
    await pipeline(res.Body, awslambda.HttpResponseStream.from(responseStream, {
        statusCode: 200,
        headers: {
            "Content-Type": res.ContentType,
        },
    }));
});
