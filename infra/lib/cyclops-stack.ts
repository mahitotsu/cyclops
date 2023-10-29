import { CfnOutput, Duration, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Architecture, Code, Function, FunctionUrlAuthType, LambdaInsightsVersion, Runtime, Tracing } from "aws-cdk-lib/aws-lambda";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const webapp = new Function(this, 'Webapp', {
            runtime: Runtime.NODEJS_LATEST,
            handler: 'server/index.handler',
            code: Code.fromDockerBuild(`${__dirname}/../../webapp`, {
                imagePath: '/workdir/.output',
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