import { GetObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { handler as frontend } from '../01_frontend/.output/server/index.mjs';
import { promisify } from 'node:util';
import { Readable, pipeline } from 'node:stream';
import * as AWSXRay from 'aws-xray-sdk';

const bucketName = process.env.BUCKET_NAME;
const keyPrefix = process.env.KEY_PREFIX;

const s3 = (async () => AWSXRay.captureAWSv3Client(new S3Client()))();

exports.handler = awslambda.streamifyResponse(async (event, responseStream, context) => {

    console.log(JSON.stringify(event, null, 4));
    console.log(JSON.stringify(context, null, 4));

    const rawPath = event.rawPath;

    // static contents
    if (rawPath && rawPath.split('/').pop().indexOf('.') > 0) {
        return s3.then(client => client.send(new GetObjectCommand({
            Bucket: bucketName,
            Key: `${keyPrefix}${rawPath}`,
        }))).then(result => promisify(pipeline)(
            result.Body,
            awslambda.HttpResponseStream.from(responseStream, {
                statusCode: 200,
                headers: {
                    'Content-Type': result.ContentType,
                    'Content-Length': result.ContentLength,
                    'ETag': result.ETag,
                    'Expires': result.Expires,
                    'LastModified': result.LastModified,
                },
            })
        ));
    }

    // process server-side rendering
    return frontend(event, context).then(res => promisify(pipeline)(
        Readable.from([res.body]),
        awslambda.HttpResponseStream.from(responseStream, {
            statusCode: res.statusCode,
            headers: res.headers,
        })
    ));
});