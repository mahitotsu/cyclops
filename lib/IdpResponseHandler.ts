import { APIGatewayProxyHandler } from "aws-lambda";

export const handler: APIGatewayProxyHandler = async (event, context) => {
    return {
        statusCode: 302,
        headers: {
            'Location': '/',
        },
        body: '',
    }
}