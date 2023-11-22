import { CfnOutput, DockerImage, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Code, Function, FunctionUrlAuthType, InvokeMode, Runtime } from "aws-cdk-lib/aws-lambda";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { Construct } from "constructs";
import * as os from 'node:os';

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const frontend = new Function(this, 'Frontend', {
            runtime: Runtime.NODEJS_20_X,
            code: Code.fromAsset(`${__dirname}/../frontend`, {
                bundling: {
                    image: DockerImage.fromRegistry('public.ecr.aws/docker/library/node:20'),
                    volumes: [ { containerPath: '/.npm', hostPath: `${os.homedir()}/.npm` }, ],
                    command: ['bash', '-c', [
                        'npm i',
                        'npm run build',
                        './node_modules/.bin/esbuild index.mjs --bundle --platform=node --minify --outdir=/asset-output',
                        'cp -r ./.output/public /asset-output',
                    ].join(' && ')]
                }
            }),
            handler: 'index.handler',
            memorySize: 128,
        });
        new LogGroup(frontend, 'LogGroup', {
            logGroupName: `/aws/lambda/${frontend.functionName}`,
            retention: RetentionDays.ONE_DAY,
            removalPolicy: RemovalPolicy.DESTROY,
        });

        const frontUrl = frontend.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
            invokeMode: InvokeMode.RESPONSE_STREAM,
        });
        new CfnOutput(this, 'FrontUrl', { value: frontUrl.url });
    }
}