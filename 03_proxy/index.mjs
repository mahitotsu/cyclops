import { GetObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { handler as frontend } from '../01_frontend/.output/server/index.mjs';
import { promisify } from 'node:util';
import { Readable, pipeline } from 'node:stream';

const bucketName = process.env.BUCKET_NAME;
const keyPrefix = process.env.KEY_PREFIX;

const s3 = (async () => new S3Client())();

exports.handler = awslambda.streamifyResponse(async (event, responseStream, _context) => {

    const rawPath = event.rawPath;
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
                },
            })
        ));
    }

    return frontend(event, _context).then(res => promisify(pipeline)(
        Readable.from([res.body]),
        awslambda.HttpResponseStream.from(responseStream, {
            statusCode: res.statusCode,
            headers: res.headers,
        })
    ));
});