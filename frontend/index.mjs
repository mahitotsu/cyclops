import { handler as nuxt3 } from './.output/server/index.mjs'
import util from 'node:util';
import stream from 'node:stream';
import fs from 'node:fs';

const pipeline = util.promisify(stream.pipeline);

export const handler = awslambda.streamifyResponse(
    async (event, responseStream, context) => {

        // provide static contents
        if (event.rawPath.startsWith('/_nuxt')) {
            const readStream = fs.createReadStream('./public${event.rawPath}');
            responseStream = awslambda.HttpResponseStream.from(responseStream, { statusCode: 200, });
            return pipeline(readStream, responseStream);
        }

        // invoke the server side logics and response the result
        return nuxt3(event).then(res => {
            const { statusCode, headers, body } = res;
            responseStream = awslambda.HttpResponseStream.from(responseStream, { statusCode, headers });
            return pipeline(body, responseStream);
        });
    }
);