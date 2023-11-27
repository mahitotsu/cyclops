import { CfnOutput, RemovalPolicy, Stack, StackProps } from "aws-cdk-lib";
import { ArnPrincipal, Effect, PolicyStatement } from "aws-cdk-lib/aws-iam";
import { FunctionUrlAuthType, InvokeMode, Runtime } from "aws-cdk-lib/aws-lambda";
import { NodejsFunction } from "aws-cdk-lib/aws-lambda-nodejs";
import { BlockPublicAccess, Bucket } from "aws-cdk-lib/aws-s3";
import { BucketDeployment, Source } from "aws-cdk-lib/aws-s3-deployment";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

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

        const webServer = new NodejsFunction(this, 'WebServer', {
            runtime: Runtime.NODEJS_LATEST,
            entry: `${__dirname}/WebServer.mjs`,
            environment: {
                BUCKET_WEBSITE_URL: bucket.bucketWebsiteUrl,
                WEBSITE_PATH_PREFIX: '/dist',
            },
        });
        const location = webServer.addFunctionUrl({
            authType: FunctionUrlAuthType.NONE,
            invokeMode: InvokeMode.BUFFERED,
        });
        bucket.addToResourcePolicy(new PolicyStatement({
            effect: Effect.ALLOW,
            principals: [new ArnPrincipal(webServer.role!.roleArn)],
            actions: ['s3:GetObject'],
            resources: [bucket.arnForObjects('*')],
        }));

        new CfnOutput(this, 'EntryLocation', { value: location.url });
        new CfnOutput(this, 'OriginLocation', { value: bucket.bucketWebsiteUrl });
    }
}