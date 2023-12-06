import { handler as server } from './.output/server/index.mjs'
import { createReadStream } from 'node:fs';
import { Readable } from 'node:stream'
import { pipeline } from 'node:stream/promises'

export const handler = awslambda.streamifyResponse(
    async (event, responseStream, context) => {

        const rawPath = event.rawPath;

        if (rawPath.startsWith('/_nuxt') || rawPath == '/favicon.ico') {
            return pipeline(
                createReadStream(`./.output/public${rawPath}`),
                awslambda.HttpResponseStream.from(responseStream, {
                    statusCode: 200,
                })
            );
        }

        return server(event, context).then(res =>
            pipeline(
                Readable.from(res.body),
                awslambda.HttpResponseStream.from(responseStream, {
                    statusCode: res.statusCode,
                    headers: res.headers,
                })
            )
        );
    }
);