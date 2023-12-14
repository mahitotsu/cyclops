import { CfnOutput, DockerImage, Fn, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Certificate } from "aws-cdk-lib/aws-certificatemanager";
import { AllowedMethods, CachePolicy, Distribution, KeyGroup, OriginRequestPolicy, PublicKey, ResponseHeadersPolicy, ViewerProtocolPolicy } from "aws-cdk-lib/aws-cloudfront";
import { HttpOrigin } from "aws-cdk-lib/aws-cloudfront-origins";
import { FunctionUrlAuthType, InvokeMode, LambdaInsightsVersion, Runtime, Tracing } from "aws-cdk-lib/aws-lambda";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { ARecord, PublicHostedZone, RecordTarget } from "aws-cdk-lib/aws-route53";
import { CloudFrontTarget } from "aws-cdk-lib/aws-route53-targets";
import { Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { StringParameter } from "aws-cdk-lib/aws-ssm";
import { Construct } from "constructs";
import * as os from 'node:os';

export class WebappStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const hostedZone = PublicHostedZone.fromHostedZoneAttributes(this, 'HostedZone', {
            hostedZoneId: 'Z00285912B2ULPDZAM9V9', zoneName: 'mahitotsu.com'
        });
        const certificate = Certificate.fromCertificateArn(this, 'Certificate',
            `arn:aws:acm:us-east-1:${props.env?.account}:certificate/053dc7b0-3805-42bd-8d17-28db8cc027bc`);
        const pubKeyStr = StringParameter.fromStringParameterName(this, 'PublicKeyStr',
            '/keypair/mahitotsu/public').stringValue;
        const webSubDomain = 'www';
        const authSubDomain = 'auth';

        const bucket = new Bucket(this, 'Bucket', {
            autoDeleteObjects: true,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        const keyPrefix = 'public';
        new BucketDeployment(bucket, 'Deployment', {
            destinationBucket: bucket,
            destinationKeyPrefix: keyPrefix,
            sources: [Source.asset(`${__dirname}/../01_frontend/`, {
                bundling: {
                    image: DockerImage.fromRegistry('public.ecr.aws/docker/library/node:18'),
                    volumes: [{ containerPath: '/.npm', hostPath: `${os.homedir()}/.npm}` }],
                    workingDirectory: '/asset-input',
                    command: ['bash', '-c', [
                        'npm install',
                        'npm run build',
                        'cp -r /asset-input/.output/public/* /asset-output'
                    ].join(' && ')],
                }
            })],
            retainOnDelete: false,
        });

        const proxy = new NodejsFunction(this, 'Proxy', {
            runtime: Runtime.NODEJS_18_X,
            entry: `${__dirname}/../03_proxy/index.mjs`,
            memorySize: 256,
            environment: {
                BUCKET_NAME: bucket.bucketName,
                KEY_PREFIX: keyPrefix,
            },
            tracing: Tracing.ACTIVE,
            insightsVersion: LambdaInsightsVersion.VERSION_1_0_229_0,
            bundling: {
                minify: true,
            }
        });
        bucket.grantRead(proxy);
        new LogGroup(proxy, 'LogGroup', {
            logGroupName: `/aws/lambda/${proxy.functionName}`,
            retention: RetentionDays.ONE_DAY,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        const location = proxy.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
            invokeMode: InvokeMode.RESPONSE_STREAM,
        });

        const pubKey = new PublicKey(this, 'PublicKey', { encodedKey: pubKeyStr, });
        const keyGroup = new KeyGroup(this, 'KeyGroup', { items: [pubKey], });
        const distribution = new Distribution(this, 'Distribution', {
            domainNames: [`${webSubDomain}.${hostedZone.zoneName}`], certificate,
            defaultBehavior: {
                origin: new HttpOrigin(Fn.parseDomainName(location.url)),
                allowedMethods: AllowedMethods.ALLOW_ALL,
                cachePolicy: CachePolicy.CACHING_DISABLED,
                originRequestPolicy: OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER,
                viewerProtocolPolicy: ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
                trustedKeyGroups: [keyGroup]
            },
        });
        const distRecord = new ARecord(distribution, 'DomainRecord', {
            recordName: webSubDomain, zone: hostedZone,
            target: RecordTarget.fromAlias(new CloudFrontTarget(distribution)),
        });

        new CfnOutput(this, 'ProxyLocation', { value: `https://${distRecord.domainName}/` });
    }
}