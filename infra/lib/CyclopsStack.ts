import { CfnOutput, DockerImage, Duration, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Code, Function, FunctionUrlAuthType, Runtime } from "aws-cdk-lib/aws-lambda";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { Construct } from "constructs";
import * as os from 'os';

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const webapp = new Function(this, 'Webapp', {
            runtime: Runtime.JAVA_17,
            code: Code.fromAsset(`${__dirname}/../../webapp`, {
                bundling: {
                    image: DockerImage.fromBuild(`${__dirname}/../builder`),
                    volumes: [{
                        hostPath: `${os.homedir()}/.m2`, containerPath: '/var/maven/.m2'
                    }, {
                        hostPath: `${os.homedir()}/.npm`, containerPath: '/.npm'
                    }],
                    environment: {
                        MAVEN_CONFIG: '/var/maven/.m2',
                    },
                    command: ['bash', '-c', [
                        'mvn clean -Duser.home=/var/maven',
                        'npm --prefix src/main/nuxt3 run build',
                        'mkdir -p ./target/classes/public',
                        'cp -R ./src/main/nuxt3/dist/* ./target/classes/public/',
                        'mvn package -DfinalName=webapp -Duser.home=/var/maven',
                        'cp ./target/webapp.jar /asset-output'
                    ].join(' && ')],
                }
            }),
            handler: 'com.mahitotsu.cyclops.webapp.Handler::handleRequest',
            memorySize: 256,
            timeout: Duration.seconds(10),
        });
        new LogGroup(webapp, 'LogGroup', {
            logGroupName: `/aws/lambda/${webapp.functionName}`,
            retention: RetentionDays.ONE_DAY,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        const endpoint = webapp.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
        });

        new CfnOutput(this, 'WebappEndpointUrl', { value: endpoint.url });
    }
}