import { CfnOutput, DockerImage, Duration, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Architecture, Code, Function, FunctionUrlAuthType, LambdaInsightsVersion, Runtime, Tracing } from "aws-cdk-lib/aws-lambda";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const webapp = new Function(this, 'Webapp', {
            runtime: Runtime.NODEJS_LATEST,
            handler: 'public/index.handler',
            code: Code.fromAsset(`${__dirname}/../../webapp`, {
                bundling: {
                    image: DockerImage.fromRegistry('public.ecr.aws/docker/library/node:18'),
                    workingDirectory: '/asset-input',
                    user: 'root',
                    command: ['bash', '-c', [
                        'npm run build',
                        'cp -r /asset-input/.output/* /asset-output/',
                    ].join(' && ')],
                },
            }),
            memorySize: 256,
            timeout: Duration.seconds(5),
            insightsVersion: LambdaInsightsVersion.VERSION_1_0_229_0,
            architecture: Architecture.ARM_64,
            tracing: Tracing.ACTIVE,
        });
        new LogGroup(webapp, 'LogGroup', {
            logGroupName: `/aws/lambda/${webapp.functionName}`,
            retention: RetentionDays.ONE_DAY,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        const webappUrl = webapp.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
        });

        new CfnOutput(this, 'WebappUrl', { value: webappUrl.url });
    }
}