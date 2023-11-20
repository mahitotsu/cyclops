import { CfnOutput, Duration, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Certificate } from "aws-cdk-lib/aws-certificatemanager";
import { AccountRecovery, Mfa, OAuthScope, UserPool, UserPoolDomain } from "aws-cdk-lib/aws-cognito";
import { Peer, Port, SecurityGroup, Vpc } from "aws-cdk-lib/aws-ec2";
import { Cluster, ContainerImage, FargateService, FargateTaskDefinition, LogDriver } from "aws-cdk-lib/aws-ecs";
import { CfnLoadBalancer, NetworkLoadBalancer, NetworkTargetGroup, Protocol, TargetType } from "aws-cdk-lib/aws-elasticloadbalancingv2";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { ARecord, PublicHostedZone, RecordTarget } from "aws-cdk-lib/aws-route53";
import { LoadBalancerTarget } from "aws-cdk-lib/aws-route53-targets";
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
        const springPidFile = '/var/run/application.pid';
        const serverSubdomain = 'www';
        const serverDomainName = `${serverSubdomain}.${hostedZone.zoneName}`

        const userPool = UserPool.fromUserPoolId(this, 'UserPool', 'ap-northeast-1_Hk6zsJaNX');
        const userPoolDomain = UserPoolDomain.fromDomainName(userPool, 'UserPoolDomain', 'https://auth.mahitotsu.com');
        const userPoolClient = userPool.addClient('CyclopsClient', {
            generateSecret: true,
            oAuth: {
                flows: { authorizationCodeGrant: true, implicitCodeGrant: false },
                scopes: [OAuthScope.OPENID, OAuthScope.EMAIL],
                callbackUrls: [`https://${serverDomainName}/login/oauth2/code/cognito`],
            }
        });

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
            environment: {
                SERVER_PORT: containerPort.toString(),
                SPRING_PID_FILE: springPidFile,
            },
            healthCheck: {
                command: ['CMD-SHELL', `test -e ${springPidFile}`],
                interval: Duration.seconds(5),
                timeout: Duration.seconds(2),
                startPeriod: Duration.seconds(30),
                retries: 6,
            },
            startTimeout: Duration.seconds(60),
            stopTimeout: Duration.seconds(30),
        });

        const service = new FargateService(cluster, 'ServerService', {
            cluster, taskDefinition, assignPublicIp: true,
            desiredCount: 1,
            capacityProviderStrategies: [{ capacityProvider: 'FARGATE_SPOT', base: 0, weight: 1 }],
            circuitBreaker: { rollback: true }, maxHealthyPercent: 200, minHealthyPercent: 0,
        });

        const nlbSg = new SecurityGroup(vpc, 'NLBSg', { vpc, allowAllOutbound: false });
        const loadBalancer = new NetworkLoadBalancer(vpc, 'NLB', {
            vpc, internetFacing: true,
        });
        (loadBalancer.node.defaultChild as CfnLoadBalancer).addPropertyOverride(
            'SecurityGroups', [nlbSg.securityGroupId],
        );
        nlbSg.connections.allowTo(service, Port.tcp(containerPort));
        service.connections.allowFrom(nlbSg, Port.tcp(containerPort));

        const targetGroup = new NetworkTargetGroup(vpc, 'TargetGroup', {
            vpc, port: containerPort, targetType: TargetType.IP,
            targets: [service],
            healthCheck: {
                interval: Duration.seconds(5),
                timeout: Duration.seconds(2),
                healthyThresholdCount: 2,
                unhealthyThresholdCount: 2,
            },
            deregistrationDelay: Duration.seconds(0),
            connectionTermination: true,
        });

        const listenerPort = 443;
        loadBalancer.addListener('ServerListener', {
            port: listenerPort, protocol: Protocol.TLS, certificates: [certificate],
            defaultTargetGroups: [targetGroup],
        });
        nlbSg.connections.allowFrom(Peer.anyIpv4(), Port.tcp(listenerPort));

        new ARecord(hostedZone, 'ServerRecord', {
            zone: hostedZone, recordName: serverSubdomain,
            target: RecordTarget.fromAlias(new LoadBalancerTarget(loadBalancer)),
        });
        new CfnOutput(this, 'ServerUrl', { value: `https://${serverDomainName}:${listenerPort}/` });
    }
}