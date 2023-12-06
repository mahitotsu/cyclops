import { CfnOutput, DockerImage, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Code, DockerImageCode, DockerImageFunction, Function, FunctionUrlAuthType, InvokeMode, Runtime } from "aws-cdk-lib/aws-lambda";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const webapp = new Function(this, 'webapp', {
            runtime: Runtime.NODEJS_20_X,
            code: Code.fromAsset(`${__dirname}/../frontend`, {
                bundling: {
                    image: DockerImage.fromRegistry('public.ecr.aws/docker/library/node:20'),
                    command: ['bash', '-c', [
                        'npm install',
                        'npm run build',
                        'cp -r /asset-input/.output /asset-output',
                        'cp /asset-input/index.mjs /asset-output',
                    ].join(' && ')],
                }
            }),
            handler: 'index.handler',
            memorySize: 256,
        });
        new LogGroup(webapp, 'LogGroup', {
            logGroupName: `/aws/lambda/${webapp.functionName}`,
            retention: RetentionDays.ONE_DAY,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        const entryLocation = webapp.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
            invokeMode: InvokeMode.RESPONSE_STREAM,
        });

        new CfnOutput(this, 'EntryLocation', { value: entryLocation.url });
    }
}