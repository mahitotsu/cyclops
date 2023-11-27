const bucketWebsiteUrl = process.env.BUCKET_WEBSITE_URL ?? 'http://localhost';
const websitePathPrefix = process.env.WEBSITE_PATH_PREFIX ?? '';

export const handler = async (event, context) => {

    const method = event.requestContext.http.method;
    if ('GET' != method) {
        return new Promise(resolve => {
            return {
                statusCode: 405, // method not allowed
                body: '',
            };
        });
    }

    const rawPath = event.rawPath;
    const path = rawPath.lastIndexOf('.') > 0 ? rawPath : '/';

    const response = {};
    return fetch(`${bucketWebsiteUrl}${websitePathPrefix}${path}`).then(res => {
        response.statusCode = res.status;
        response.headers = {};
        for (const entry of res.headers) {
            response.headers[entry[0]] = entry[1];
        }
        return res.text();
    }).then(text => {
        response.body = text;
        return response;
    });
}