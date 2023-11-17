import { RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Certificate } from "aws-cdk-lib/aws-certificatemanager";
import { Vpc } from "aws-cdk-lib/aws-ec2";
import { Cluster, ContainerImage, FargateTaskDefinition, LogDriver } from "aws-cdk-lib/aws-ecs";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { PublicHostedZone } from "aws-cdk-lib/aws-route53";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const vpc = Vpc.fromLookup(this, 'Vpc', { vpcId: 'vpc-0316bba888afa43c5' });
        const cluster = Cluster.fromClusterAttributes(vpc, 'Cluster', { clusterName: 'default', vpc, });
        const hostedZone = PublicHostedZone.fromHostedZoneAttributes(this, 'PublicHostedZone', {
            hostedZoneId: 'Z00285912B2ULPDZAM9V9', zoneName: 'mahitotsu.com',
        });
        const certificate = Certificate.fromCertificateArn(hostedZone, 'Certificate',
            'arn:aws:acm:ap-northeast-1:346929044083:certificate/da416a14-d9d8-42fb-9d91-9197d8f78d80');

        const containerPort = 8080;
        const taskDefinition = new FargateTaskDefinition(this, 'ServerTaskDefinition', {
            cpu: 1024, memoryLimitMiB: 2048,
        });
        const main = taskDefinition.addContainer('main', {
            image: ContainerImage.fromAsset(`${__dirname}/../../server`),
            portMappings: [{ containerPort, }],
            logging: LogDriver.awsLogs({
                streamPrefix: 'Service',
                logGroup: new LogGroup(this, 'ServerLogGroup', {
                    retention: RetentionDays.ONE_DAY,
                    removalPolicy: RemovalPolicy.DESTROY,
                })
            }),
        });
    }
}