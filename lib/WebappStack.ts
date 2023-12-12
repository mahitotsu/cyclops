import { CfnOutput, DockerImage, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Code, Function, FunctionUrlAuthType, InvokeMode, Runtime } from "aws-cdk-lib/aws-lambda";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Construct } from "constructs";
import * as os from 'node:os';

export class WebappStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const bucket = new Bucket(this, 'Bucket', {
            autoDeleteObjects: true,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        new BucketDeployment(bucket, 'Deployment', {
            destinationBucket: bucket,
            destinationKeyPrefix: 'public',
            sources: [Source.asset(`${__dirname}/../01_frontend/`, {
                bundling: {
                    image: DockerImage.fromRegistry('public.ecr.aws/docker/library/node:18'),
                    volumes: [{ containerPath: '/.npm', hostPath: `${os.homedir()}/.npm}` }],
                    workingDirectory: '/asset-input',
                    command: ['bash', '-c', [
                        'npm install',
                        'npm run build',
                        'cp -r /asset-input/.output/public /asset-output'
                    ].join(' && ')],
                }
            })],
            retainOnDelete: false,
        });

        const proxy = new NodejsFunction(this, 'Proxy', {
            runtime: Runtime.NODEJS_18_X,
            entry: `${__dirname}/../03_proxy/index.mjs`,
            bundling: {
                minify: true,
            }
        });
        new LogGroup(proxy, 'LogGroup', {
            logGroupName: `/aws/lambda/${proxy.functionName}`,
            retention: RetentionDays.ONE_DAY,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        const location = proxy.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
            invokeMode: InvokeMode.BUFFERED,
        });

        new CfnOutput(this, 'ProxyLocation', { value: location.url });
    }
}