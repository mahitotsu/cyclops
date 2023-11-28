import { CfnOutput, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { GatewayVpcEndpointAwsService, IpAddresses, Peer, Port, SubnetType, Vpc } from "aws-cdk-lib/aws-ec2";
import { AnyPrincipal, Effect, PolicyStatement } from "aws-cdk-lib/aws-iam";
import { FunctionUrlAuthType, InvokeMode, LambdaInsightsVersion, Runtime, RuntimeManagementMode, Tracing } from "aws-cdk-lib/aws-lambda";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { BlockPublicAccess, Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const vpc = new Vpc(this, 'Vpc', {
            natGateways: 0, createInternetGateway: false,
            ipAddresses: IpAddresses.cidr('192.168.0.0/24'),
            subnetConfiguration: [{ name: 'private', subnetType: SubnetType.PRIVATE_ISOLATED, }],
        });

        const bucket = new Bucket(this, 'Bucket', {
            publicReadAccess: false,
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            websiteIndexDocument: 'index.html',
            autoDeleteObjects: true,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        new BucketDeployment(bucket, 'Deployment', {
            sources: [Source.asset(`${__dirname}/../frontend/dist`)],
            destinationBucket: bucket,
            destinationKeyPrefix: 'dist/',
            retainOnDelete: false,
        });
        const s3vpce = vpc.addGatewayEndpoint('S3vpce', { service: GatewayVpcEndpointAwsService.S3 });
        bucket.addToResourcePolicy(new PolicyStatement({
            effect: Effect.ALLOW,
            principals: [new AnyPrincipal()],
            actions: ['s3:GetObject'],
            resources: [bucket.arnForObjects('*')],
            conditions: { 'StringEquals': { 'aws:SourceVpce': s3vpce.vpcEndpointId } }
        }));

        const webServer = new NodejsFunction(this, 'WebServer', {
            runtime: Runtime.NODEJS_LATEST, runtimeManagementMode: RuntimeManagementMode.AUTO,
            memorySize: 256,
            entry: `${__dirname}/WebServer.mjs`,
            environment: {
                BUCKET_WEBSITE_URL: bucket.bucketWebsiteUrl,
                WEBSITE_PATH_PREFIX: '/dist',
            },
            vpc, vpcSubnets: { subnetType: SubnetType.PRIVATE_ISOLATED },
            allowAllOutbound: false,
            tracing: Tracing.ACTIVE, insightsVersion: LambdaInsightsVersion.VERSION_1_0_229_0,
        });
        webServer.connections.allowTo(Peer.prefixList('pl-61a54008'), Port.tcp(80));
        new LogGroup(webServer, 'LogGroup', {
            logGroupName: `/aws/lambda/${webServer.functionName}`,
            retention: RetentionDays.ONE_DAY,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        const location = webServer.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
            invokeMode: InvokeMode.RESPONSE_STREAM,
        });

        new CfnOutput(this, 'EntryLocation', { value: location.url });
    }
}