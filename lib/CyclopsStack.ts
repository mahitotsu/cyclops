import { CfnOutput, Fn, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { CachePolicy, Distribution, ViewerProtocolPolicy } from "aws-cdk-lib/aws-cloudfront";
import { HttpOrigin, S3Origin } from "aws-cdk-lib/aws-cloudfront-origins";
import { Architecture, FunctionUrlAuthType, Runtime } from "aws-cdk-lib/aws-lambda";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { BlockPublicAccess, Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const server = new NodejsFunction(this, 'Server', {
            entry: `${__dirname}/../webapp/.output/server/index.mjs`,
            handler: 'handler',
            runtime: Runtime.NODEJS_LATEST,
            architecture: Architecture.ARM_64,
        });
        const endpoint = server.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
        });

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

        const dist = new Distribution(this, 'Distribution', {
            defaultBehavior: {
                origin: new HttpOrigin(Fn.parseDomainName(endpoint.url)),
                viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                cachePolicy: CachePolicy.CACHING_DISABLED,
            },
            additionalBehaviors: {
                "/_nuxt/*": {
                    origin: new S3Origin(bucket, { originPath: 'public' }),
                    viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                    cachePolicy: CachePolicy.CACHING_DISABLED,
                }
            }
        });

        new CfnOutput(this, 'Url', { value: `https://${dist.distributionDomainName}` });
    }
}