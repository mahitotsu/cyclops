import { promisify } from 'util';
import { Readable, Stream } from "stream";
const pipeline = promisify(Stream.pipeline);

const bucketWebsiteUrl = process.env.BUCKET_WEBSITE_URL ?? 'http://localhost';
const websitePathPrefix = process.env.WEBSITE_PATH_PREFIX ?? '';

export const handler = awslambda.streamifyResponse(
    async (event, responseStream, _context) => {

        if ('GET' != event.requestContext.http.method) {
            return pipeline(Readable.from(''),
                awslambda.HttpResponseStream.from(responseStream, {
                    statusCode: 405, // method not allowed
                })
            );
        }

        const rawPath = event.rawPath;
        const path = rawPath.lastIndexOf('.') > 0 ? rawPath : '/';

        return fetch(`${bucketWebsiteUrl}${websitePathPrefix}${path}`).then(res =>
            pipeline(res.body,
                awslambda.HttpResponseStream.from(responseStream, {
                    statusCode: res.status,
                    headers: Array.from(res.headers).reduce((h, i) => { h[i[0]] = i[1]; return h; }, {}),
                })
            )
        );
    }
);