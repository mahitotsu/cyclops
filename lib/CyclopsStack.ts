import { CfnOutput, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { DockerImageCode, DockerImageFunction, FunctionUrlAuthType, InvokeMode } from "aws-cdk-lib/aws-lambda";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const webapp = new DockerImageFunction(this, 'Webapp', {
            code: DockerImageCode.fromImageAsset(`${__dirname}/../frontend`),
            memorySize: 256,
        });
        new LogGroup(webapp, 'LogGroup', {
            logGroupName: `/aws/lambda/${webapp.functionName}`,
            retention: RetentionDays.ONE_DAY,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        const entryLocation = webapp.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
            invokeMode: InvokeMode.BUFFERED,
        });

        new CfnOutput(this, 'EntryLocation', { value: entryLocation.url });
    }
}