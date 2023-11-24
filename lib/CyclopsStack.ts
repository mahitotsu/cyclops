import { CfnOutput, DockerImage, Fn, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { CachePolicy, Distribution, OriginRequestPolicy, ViewerProtocolPolicy } from "aws-cdk-lib/aws-cloudfront";
import { HttpOrigin, S3Origin } from "aws-cdk-lib/aws-cloudfront-origins";
import { FunctionUrlAuthType, InvokeMode, Runtime } from "aws-cdk-lib/aws-lambda";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { BlockPublicAccess, Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Construct } from "constructs";
import * as os from 'node:os';

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const bucket = new Bucket(this, 'Bucket', {
            publicReadAccess: false,
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            autoDeleteObjects: true,
            removalPolicy: RemovalPolicy.DESTROY,
        });
        new BucketDeployment(bucket, 'Deployment', {
            sources: [Source.asset(`${__dirname}/../frontend/.output/public`)],
            destinationBucket: bucket,
            destinationKeyPrefix: 'public/',
            retainOnDelete: false,
        });

        const server = new NodejsFunction(this, 'Server', {
            entry: `${__dirname}/../frontend/.output/server/index.mjs`,
            runtime: Runtime.NODEJS_LATEST,
        });
        const entry = server.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
            invokeMode: InvokeMode.BUFFERED,
        });

        const distribution = new Distribution(this, 'Distribution', {
            defaultBehavior: {
                origin: new HttpOrigin(Fn.parseDomainName(entry.url)),
                cachePolicy: CachePolicy.CACHING_DISABLED,
                viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                originRequestPolicy: OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER,
            },
            additionalBehaviors: {
                '*.*': {
                    origin: new S3Origin(bucket, {
                        originPath: 'public'
                    }),
                    viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                    cachePolicy: CachePolicy.CACHING_DISABLED,
                }
            },
        });

        new CfnOutput(this, 'DistributedUrl', { value: `https://${distribution.domainName}/` });
    }
}