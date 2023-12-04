import { CfnOutput, DockerImage, Fn, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Certificate } from "aws-cdk-lib/aws-certificatemanager";
import { CachePolicy, CfnDistribution, CfnOriginAccessControl, Distribution, KeyGroup, OriginRequestPolicy, PublicKey, ViewerProtocolPolicy } from "aws-cdk-lib/aws-cloudfront";
import { HttpOrigin, S3Origin } from "aws-cdk-lib/aws-cloudfront-origins";
import { OAuthScope, UserPool, UserPoolDomain } from "aws-cdk-lib/aws-cognito";
import { Effect, PolicyStatement, ServicePrincipal } from "aws-cdk-lib/aws-iam";
import { FunctionUrlAuthType, InvokeMode, ParamsAndSecretsLayerVersion, ParamsAndSecretsVersions } from "aws-cdk-lib/aws-lambda";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { ARecord, PublicHostedZone, RecordTarget } from "aws-cdk-lib/aws-route53";
import { CloudFrontTarget } from "aws-cdk-lib/aws-route53-targets";
import { BlockPublicAccess, Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Secret } from "aws-cdk-lib/aws-secretsmanager";
import { SigningProfile } from "aws-cdk-lib/aws-signer";
import { StringParameter } from "aws-cdk-lib/aws-ssm";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);
        const { account, region } = props.env!;

        const bucket = new Bucket(this, 'Bucket', {
            publicReadAccess: false,
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            autoDeleteObjects: true,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        const keyPrefix = 'html';
        new BucketDeployment(bucket, 'Deployment', {
            destinationBucket: bucket,
            destinationKeyPrefix: keyPrefix,
            sources: [Source.asset(`${__dirname}/../frontend`, {
                bundling: {
                    image: DockerImage.fromRegistry('public.ecr.aws/docker/library/node:18'),
                    command: ['bash', '-c', [
                        'npm run build',
                        `cp -r ./.output/public/* /asset-output/`
                    ].join(' && ')],
                }
            })],
            retainOnDelete: false,
        });

        const frontend = new Construct(this, 'FrontEnd');
        const pubKeyParam = StringParameter.fromStringParameterName(frontend, 'PubKeyParam', '/keypair/mahitotsu/public');
        const publicKey = new PublicKey(frontend, 'PublicKey2', { encodedKey: pubKeyParam.stringValue });
        const keyGroup = new KeyGroup(frontend, 'KeyGroup', { items: [publicKey] });
        const oai = new CfnOriginAccessControl(frontend, 'OAI', {
            originAccessControlConfig: {
                name: 'OAIForFrontEndBucket',
                originAccessControlOriginType: 's3',
                signingBehavior: 'always',
                signingProtocol: 'sigv4',
            }
        });

        const hostedZone = PublicHostedZone.fromHostedZoneAttributes(this, 'HostedZone', {
            hostedZoneId: 'Z00285912B2ULPDZAM9V9', zoneName: 'mahitotsu.com',
        });
        const certificate = Certificate.fromCertificateArn(this, 'Certificate',
            `arn:aws:acm:us-east-1:${account}:certificate/053dc7b0-3805-42bd-8d17-28db8cc027bc`,
        );
        const webappSubDomain = 'www';
        const webappDomain = `${webappSubDomain}.${hostedZone.zoneName}`;
        const authDomain = `auth.${hostedZone.zoneName}`;
        const loginPath = 'oauth2/idpresponse';
        const callbackUrl = `https://${webappDomain}/${loginPath}`;

        const userPool = UserPool.fromUserPoolId(this, 'UserPool', 'ap-northeast-1_Hk6zsJaNX');
        const userDomain = UserPoolDomain.fromDomainName(userPool, 'Domain', authDomain);
        const userClient = userPool.addClient('UserClient', {
            generateSecret: true, oAuth: {
                scopes: [OAuthScope.OPENID, OAuthScope.EMAIL],
                flows: { authorizationCodeGrant: true, implicitCodeGrant: false },
                callbackUrls: [callbackUrl],
            },
        });
        const signInUrl = `https://${userDomain.domainName}/authorize?${[
            `client_id=${userClient.userPoolClientId}`,
            'response_type=code',
            'scope=openid',
            `redirect_uri=${callbackUrl}`
        ].join('&')}`;

        const privateKeySecretName = '/keypair/mahitotsu/private';
        const signer = new NodejsFunction(this, 'Signer', {
            entry: `${__dirname}/Signer.mjs`,
            paramsAndSecrets: ParamsAndSecretsLayerVersion.fromVersion(ParamsAndSecretsVersions.V1_0_103),
            environment: {
                CLOUDFRONT_KEY_PAIR_ID: publicKey.publicKeyId,
                PRIVATE_KEY_SECRET_NAME: privateKeySecretName,
                CLOUD_FRONT_DOMAIN: webappDomain,
                SIGNIN_URL: signInUrl,
            },
        });
        Secret.fromSecretNameV2(signer, 'PrivateKeySecret', privateKeySecretName).grantRead(signer);
        const signerUrl = signer.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
            invokeMode: InvokeMode.BUFFERED,
        });
        new CfnOutput(this, 'SignerLocation', { value: signerUrl.url });

        const distribution = new Distribution(this, 'Distribution', {
            defaultBehavior: {
                origin: new S3Origin(bucket, { originPath: keyPrefix, }),
                trustedKeyGroups: [keyGroup],
                cachePolicy: CachePolicy.CACHING_DISABLED,
                viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                originRequestPolicy: OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER,
            },
            defaultRootObject: 'index.html',
            domainNames: [webappDomain], certificate,
        });
        const cfnDist = distribution.node.defaultChild as CfnDistribution;
        cfnDist.addPropertyOverride('DistributionConfig.Origins.0.S3OriginConfig.OriginAccessIdentity', '');
        cfnDist.addPropertyOverride('DistributionConfig.Origins.0.OriginAccessControlId', oai.attrId);
        bucket.addToResourcePolicy(new PolicyStatement({
            effect: Effect.ALLOW,
            principals: [new ServicePrincipal('cloudfront.amazonaws.com')],
            actions: ['s3:GetObject'],
            resources: [bucket.arnForObjects('*')],
            conditions: {
                'StringEquals': {
                    'AWS:SourceArn': `arn:aws:cloudfront::${account}:distribution/${distribution.distributionId}`
                }
            }
        }));

        distribution.addBehavior(`/${loginPath}`, new HttpOrigin(Fn.parseDomainName(signerUrl.url)), {
            cachePolicy: CachePolicy.CACHING_DISABLED,
            viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
            originRequestPolicy: OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER,
        });
        const webappRecord = new ARecord(distribution, 'WebappRecord', {
            recordName: webappSubDomain, zone: hostedZone,
            target: RecordTarget.fromAlias(new CloudFrontTarget(distribution)),
        });

        new CfnOutput(this, 'WebappLocation', { value: `https://${webappDomain}/${loginPath}` });
    }
}