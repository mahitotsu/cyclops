import { handler as nuxt3 } from '../server/index.mjs';
import * as fs from 'fs';

export const handler = async (event) => {
    if (event.rawPath.startsWith('/_nuxt')) {
        const content = fs.readFileSync(`./public${event.rawPath}`)
        return {
            statusCode: 200,
            body: content.toString(),
        }
    } else if (event.rawPath == '/favicon.ico') {
        return {
            statusCode: 200,
            isBase64Encoded: true,
            headers: {
                'Content-Type': 'image/x-icon',
            },
            body: 'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQEAYAAABPYyMiAAAABmJLR0T///////8JWPfcAAAACXBIWXMAAABIAAAASABGyWs+AAAAF0lEQVRIx2NgGAWjYBSMglEwCkbBSAcACBAAAeaR9cIAAAAASUVORK5CYII=',
        }
    } else {
        return nuxt3({ rawPath: '/' });
    }
}