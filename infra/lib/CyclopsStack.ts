import { CfnOutput, DockerImage, Fn, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { CachePolicy, Distribution, OriginRequestPolicy, ViewerProtocolPolicy } from "aws-cdk-lib/aws-cloudfront";
import { HttpOrigin, S3Origin } from "aws-cdk-lib/aws-cloudfront-origins";
import { Architecture, Code, Function, FunctionUrlAuthType, Runtime } from "aws-cdk-lib/aws-lambda";
import { BlockPublicAccess, Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Construct } from "constructs";
import * as os from 'os';

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const bucket = new Bucket(this, 'Bucket', {
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            publicReadAccess: false,
            removalPolicy: RemovalPolicy.DESTROY,
            autoDeleteObjects: true,
        });
        new BucketDeployment(bucket, 'Deployment', {
            destinationBucket: bucket,
            destinationKeyPrefix: '',
            sources: [Source.asset(`${__dirname}/../../webapp`, {
                bundling: {
                    image: DockerImage.fromRegistry('public.ecr.aws/docker/library/node:18-slim'),
                    volumes: [{ containerPath: '/.npm', hostPath: `${os.homedir()}/.npm`, }],
                    command: ['bash', '-c', [
                        'npm run build',
                        'cp -r /asset-input/.output/public/_nuxt /asset-output',
                    ].join(' && ')],
                }
            })],
            retainOnDelete: false,
        });

        const webapp = new Function(this, 'Webapp', {
            runtime: Runtime.NODEJS_18_X,
            architecture: Architecture.ARM_64,
            memorySize: 256,
            code: Code.fromAsset(`${__dirname}/../../webapp/.output/server`),
            handler: 'index.handler',
            reservedConcurrentExecutions: 3,
        });
        const weburl = webapp.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
        });

        const distribution = new Distribution(this, 'Distribution', {
            defaultBehavior: {
                origin: new HttpOrigin(Fn.parseDomainName(weburl.url)),
                cachePolicy: CachePolicy.CACHING_DISABLED,
                viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                originRequestPolicy: OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER,
            },
            additionalBehaviors: {
                '/_nuxt/*': {
                    origin: new S3Origin(bucket, { originPath: '',  }),
                    cachePolicy: CachePolicy.CACHING_DISABLED,
                    viewerProtocolPolicy: ViewerProtocolPolicy.HTTPS_ONLY,
                    originRequestPolicy: OriginRequestPolicy.ALL_VIEWER_EXCEPT_HOST_HEADER,
                }
            }
        });

        new CfnOutput(this, 'entrypoint', { value: `https://${distribution.domainName}` });
    }
}