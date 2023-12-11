import { CfnOutput, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Code, Function, FunctionUrlAuthType, InvokeMode, Runtime } from "aws-cdk-lib/aws-lambda";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Construct } from "constructs";

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
            sources: [Source.asset(`${__dirname}/../01_frontend/.output/public`)],
        });

        const proxy = new Function(this, 'Proxy', {
            runtime: Runtime.NODEJS_18_X,
            code: Code.fromAsset(`${__dirname}/../03_proxy/dist`),
            handler: 'index.handler',
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