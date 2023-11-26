import { CfnOutput, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { Certificate } from "aws-cdk-lib/aws-certificatemanager";
import { CachePolicy, CfnDistribution, CfnOriginAccessControl, Distribution, KeyGroup, PublicKey, ViewerProtocolPolicy } from "aws-cdk-lib/aws-cloudfront";
import { S3Origin } from "aws-cdk-lib/aws-cloudfront-origins";
import { Effect, PolicyStatement, ServicePrincipal } from "aws-cdk-lib/aws-iam";
import { ARecord, PublicHostedZone, RecordTarget } from "aws-cdk-lib/aws-route53";
import { CloudFrontTarget } from "aws-cdk-lib/aws-route53-targets";
import { BlockPublicAccess, Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Construct } from "constructs";
import * as fs from 'node:fs';
import * as os from 'node:os';

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const hostedZone = PublicHostedZone.fromHostedZoneAttributes(this, 'HostedZone', {
            hostedZoneId: 'Z00285912B2ULPDZAM9V9', zoneName: 'mahitotsu.com',
        });
        const certificate = Certificate.fromCertificateArn(this, 'Certificate',
            `arn:aws:acm:us-east-1:${props.env?.account}:certificate/053dc7b0-3805-42bd-8d17-28db8cc027bc`);

        const bucket = new Bucket(this, 'Bucket', {
            publicReadAccess: false,
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            autoDeleteObjects: true,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        new BucketDeployment(bucket, 'Deployment', {
            sources: [Source.asset(`${__dirname}/../frontend/dist`)],
            destinationBucket: bucket,
            destinationKeyPrefix: 'dist/',
            retainOnDelete: false,
        });

        const subdomain = 'www';
        const oac = new CfnOriginAccessControl(this, 'OAC', {
            originAccessControlConfig: {
                name: 'OriginAccessControl',
                originAccessControlOriginType: 's3',
                signingBehavior: 'always',
                signingProtocol: 'sigv4',
            }
        });
        const publicKey = new PublicKey(oac, 'PublicKey', {
            encodedKey: fs.readFileSync(`${os.homedir()}/keys/mahitotsu_pub_key.pem`).toString(),
        });
        const keyGroups = new KeyGroup(oac, 'KeyGroups', { items: [publicKey] });

        const distribution = new Distribution(this, 'Distribution', {
            domainNames: [`${subdomain}.${hostedZone.zoneName}`], certificate,
            defaultBehavior: {
                origin: new S3Origin(bucket, {
                    originPath: 'dist',
                }),
                viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                cachePolicy: CachePolicy.CACHING_DISABLED,
                trustedKeyGroups: [keyGroups],
            },
            defaultRootObject: 'index.html',
        });
        const cfnDist = distribution.node.defaultChild as CfnDistribution;
        cfnDist.addPropertyOverride('DistributionConfig.Origins.0.S3OriginConfig.OriginAccessIdentity', '');
        cfnDist.addPropertyOverride('DistributionConfig.Origins.0.OriginAccessControlId', oac.attrId);
        bucket.addToResourcePolicy(new PolicyStatement({
            effect: Effect.ALLOW,
            principals: [new ServicePrincipal('cloudfront.amazonaws.com')],
            actions: ['s3:GetObject'],
            resources: [bucket.arnForObjects('*')],
            conditions: {
                'StringEquals': {
                    'AWS:SourceArn': `arn:aws:cloudfront::${props.env?.account}:distribution/${distribution.distributionId}`
                }
            }
        }));

        const record = new ARecord(distribution, 'Record', {
            zone: hostedZone, recordName: subdomain,
            target: RecordTarget.fromAlias(new CloudFrontTarget(distribution)),
        });
        new CfnOutput(this, 'DistributedUrl', { value: `https://${record.domainName}/` });
    }
}