import { CfnOutput, Fn, RemovalPolicy, ScopedAws, SecretValue, Stack, StackProps } from "aws-cdk-lib";
import { CachePolicy, Distribution, OriginRequestPolicy, ViewerProtocolPolicy } from "aws-cdk-lib/aws-cloudfront";
import { HttpOrigin, S3Origin } from "aws-cdk-lib/aws-cloudfront-origins";
import { Mfa, OAuthScope, UserPool } from "aws-cdk-lib/aws-cognito";
import { Effect, PolicyStatement } from "aws-cdk-lib/aws-iam";
import { Architecture, FunctionUrlAuthType, Runtime } from "aws-cdk-lib/aws-lambda";
import { NodejsFunction, OutputFormat } from "aws-cdk-lib/aws-lambda-nodejs";
import { BlockPublicAccess, Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Secret } from "aws-cdk-lib/aws-secretsmanager";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);
        const { region, accountId } = new ScopedAws(this);

        const server = new NodejsFunction(this, 'Server', {
            entry: `${__dirname}/../webapp/.output/server/index.mjs`,
            handler: 'handler',
            runtime: Runtime.NODEJS_LATEST,
            memorySize: 256,
            architecture: Architecture.ARM_64,
            bundling: {
                banner: "import { createRequire } from 'module';const require = createRequire(import.meta.url);",
                format: OutputFormat.ESM,
            },
        });
        const appEndpoint = server.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
        });

        const authSecretName = `${server.functionName}-Secrets`;
        const authorizer = new NodejsFunction(this, 'Authorizer', {
            entry: `${__dirname}/IdpResponseHandler.ts`,
            runtime: Runtime.NODEJS_LATEST,
            memorySize: 256,
            architecture: Architecture.ARM_64,
            environment: {
                AUTH_SECRET_NAME: authSecretName,
            },
            bundling: {
                banner: "import { createRequire } from 'module';const require = createRequire(import.meta.url);",
                format: OutputFormat.ESM,
            },
        });
        const authEndpoint = authorizer.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
        });
        authorizer.addToRolePolicy(new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ['secretsmanager:DescribeSecret', 'secretsmanager:GetSecretValue'],
            resources: [`arn:aws:secretsmanager:${region}:${accountId}:secret:${authSecretName}-*`],
        }));

        const bucket = new Bucket(this, 'Bucket', {
            publicReadAccess: false,
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            removalPolicy: RemovalPolicy.DESTROY,
            autoDeleteObjects: true,
        });
        new BucketDeployment(bucket, 'BucketDeployment', {
            sources: [Source.asset(`${__dirname}/../webapp/.output/public`)],
            destinationBucket: bucket,
            destinationKeyPrefix: 'public'
        });

        const distribution = new Distribution(this, 'Distribution', {
            defaultBehavior: {
                origin: new HttpOrigin(Fn.parseDomainName(appEndpoint.url)),
                viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                cachePolicy: CachePolicy.CACHING_DISABLED,
                originRequestPolicy: OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER,
            },
            additionalBehaviors: {
                "/_nuxt/*": {
                    origin: new S3Origin(bucket, { originPath: 'public' }),
                    viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                    cachePolicy: CachePolicy.CACHING_DISABLED,
                    originRequestPolicy: OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER,
                },
                "/oauth2/idpresponse": {
                    origin: new HttpOrigin(Fn.parseDomainName(authEndpoint.url)),
                    viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                    cachePolicy: CachePolicy.CACHING_DISABLED,
                    originRequestPolicy: OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER,
                }
            }
        });

        const authUrl = `https://${distribution.domainName}/oauth2/idpresponse`;
        const userPool = new UserPool(this, 'UserPool', {
            selfSignUpEnabled: false,
            signInAliases: { email: true },
            removalPolicy: RemovalPolicy.DESTROY,
            mfa: Mfa.REQUIRED,
            mfaSecondFactor: { sms: false, otp: true },
        });
        const authDomain = userPool.addDomain('Domain', {
            cognitoDomain: { domainPrefix: Fn.select(0, Fn.split('.', distribution.domainName)) },
        });
        const client = userPool.addClient('Client', {
            generateSecret: true,
            oAuth: {
                callbackUrls: [authUrl],
                scopes: [OAuthScope.OPENID],
            }
        });

        const secret = new Secret(authorizer, 'Secrets', {
            secretName: authSecretName,
            secretObjectValue: {
                tokenEndpoint: SecretValue.unsafePlainText(`${authDomain.baseUrl()}/oauth2/token`),
                userPoolClientId: SecretValue.unsafePlainText(client.userPoolClientId),
                userPoolClientSecret: client.userPoolClientSecret,
                distributionDomain: SecretValue.unsafePlainText(distribution.domainName),
            }
        });

        new CfnOutput(this, 'Auth00', {
            value: authDomain.signInUrl(client, { redirectUri: authUrl })
        });
    };
}