import { Duration, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Port, Vpc } from "aws-cdk-lib/aws-ec2";
import { Cluster, ContainerImage, FargateService, FargateTaskDefinition, LogDriver } from "aws-cdk-lib/aws-ecs";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const vpc = Vpc.fromLookup(this, 'Vpc', { vpcId: 'vpc-0316bba888afa43c5' });
        const cluster = Cluster.fromClusterAttributes(vpc, 'Cluster', { clusterName: 'default', vpc, });

        const taskDefinition = new FargateTaskDefinition(this, 'TaskDefinition', {
            cpu: 512, memoryLimitMiB: 1024,
        });
        const main = taskDefinition.addContainer('main', {
            image: ContainerImage.fromAsset(`${__dirname}/../../webapp`),
            portMappings: [{ containerPort: 8080 }],
            logging: LogDriver.awsLogs({
                streamPrefix: 'Service',
                logGroup: new LogGroup(cluster, 'LogGroup', {
                    retention: RetentionDays.ONE_DAY,
                    removalPolicy: RemovalPolicy.DESTROY,
                }),
            }),
            startTimeout: Duration.seconds(60),
            stopTimeout: Duration.seconds(10),
            healthCheck: {
                command: ['CMD-SHELL', 'test -e application.pid'],
                interval: Duration.seconds(5),
                timeout: Duration.seconds(3),
                retries: 10,
                startPeriod: Duration.seconds(5),
            },
        });

        const service = new FargateService(cluster, 'Service', {
            cluster, taskDefinition, assignPublicIp: true,
            capacityProviderStrategies: [{ capacityProvider: 'FARGATE_SPOT', weight: 1, base: 0 }],
            circuitBreaker: { rollback: true }, minHealthyPercent: 0, maxHealthyPercent: 200,
            desiredCount: 1,
        });
        service.connections.allowFromAnyIpv4(Port.tcp(8080));
    }
}