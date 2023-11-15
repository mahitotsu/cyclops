import { CfnOutput, DockerImage, Fn, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { AccountRecovery, Mfa, OAuthScope, UserPool } from "aws-cdk-lib/aws-cognito";
import { Scope } from "aws-cdk-lib/aws-ecs";
import { FunctionUrlAuthType, InvokeMode, LambdaInsightsVersion, Runtime, Tracing } from "aws-cdk-lib/aws-lambda";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { LogGroup, RetentionDays } from "aws-cdk-lib/aws-logs";
import { BlockPublicAccess, Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Construct } from "constructs";
import * as os from 'os';

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const keyPrefix = 'webapp';
        const bucket = new Bucket(this, 'Bucket', {
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            publicReadAccess: false,
            removalPolicy: RemovalPolicy.DESTROY,
            autoDeleteObjects: true,
        });
        new BucketDeployment(bucket, 'Nuxt3Deployment', {
            destinationBucket: bucket,
            destinationKeyPrefix: keyPrefix,
            retainOnDelete: false,
            sources: [Source.asset(`../webapp`, {
                bundling: {
                    image: DockerImage.fromRegistry('public.ecr.aws/docker/library/node:18-slim'),
                    volumes: [{ containerPath: '/.npm', hostPath: `${os.homedir()}/.npm` }],
                    command: ['bash', '-c', [
                        'npm run build', 'cp -r /asset-input/.output/public/* /asset-output/',
                    ].join(' && ')],
                }
            })],
        });

        const webServer = new NodejsFunction(this, 'WebServer', {
            entry: `${__dirname}/WebServer.mjs`,
            runtime: Runtime.NODEJS_18_X,
            environment: {
                BUCKET_NAME: bucket.bucketName,
                KEY_PREFIX: keyPrefix,
            },
            memorySize: 256,
            tracing: Tracing.ACTIVE,
            insightsVersion: LambdaInsightsVersion.VERSION_1_0_229_0,
        });
        new LogGroup(webServer, 'LogGroup', {
            logGroupName: `/aws/lambda/${webServer.functionName}`,
            retention: RetentionDays.ONE_DAY,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        bucket.grantRead(webServer);
        const webEndpoint = webServer.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
            invokeMode: InvokeMode.RESPONSE_STREAM,
        });

        const userPool = new UserPool(this, 'UserPool', {
            selfSignUpEnabled: false,
            standardAttributes: { email: { required: true, mutable: false } },
            autoVerify: { email: false },
            signInAliases: { email: true },
            mfa: Mfa.REQUIRED,
            mfaSecondFactor: { otp: true, sms: false },
            accountRecovery: AccountRecovery.EMAIL_ONLY,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        const userPoolDomain = userPool.addDomain('Domain', {
            cognitoDomain: { domainPrefix: Fn.select(0, Fn.split('.', Fn.parseDomainName(webEndpoint.url))) },
        });
        const userClient = userPool.addClient('Client', {
            generateSecret: true,
            oAuth: {
                flows: { authorizationCodeGrant: true, },
                scopes: [OAuthScope.OPENID],
                callbackUrls: [`${webEndpoint.url}oauth2/idpresponse`],
            }
        });

        new CfnOutput(this, 'WebEndpoint', { value: webEndpoint.url });
    }
}