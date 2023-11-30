import { CfnOutput, DockerImage, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { CfnDistribution, CfnOriginAccessControl, Distribution, KeyGroup, PublicKey } from "aws-cdk-lib/aws-cloudfront";
import { S3Origin } from "aws-cdk-lib/aws-cloudfront-origins";
import { Effect, PolicyStatement, ServicePrincipal } from "aws-cdk-lib/aws-iam";
import { BlockPublicAccess, Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
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
        const publicKey = new PublicKey(frontend, 'PublicKey', { encodedKey: pubKeyParam.stringValue });
        const keyGroup = new KeyGroup(frontend, 'KeyGroup', { items: [publicKey] });
        const oai = new CfnOriginAccessControl(frontend, 'OAI', {
            originAccessControlConfig: {
                name: 'OAIForFrontEndBucket',
                originAccessControlOriginType: 's3',
                signingBehavior: 'always',
                signingProtocol: 'sigv4',
            }
        })

        const distribution = new Distribution(this, 'Distribution', {
            defaultBehavior: {
                origin: new S3Origin(bucket, { originPath: keyPrefix, }),
                trustedKeyGroups: [keyGroup],
            },
            defaultRootObject: 'index.html',
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

        new CfnOutput(this, 'WebappLocation', { value: `https://${distribution.domainName}/` });
    }
}