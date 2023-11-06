import { Stack, StackProps } from "aws-cdk-lib";
import { Vpc } from "aws-cdk-lib/aws-ec2";
import { Cluster, ContainerImage, FargateTaskDefinition } from "aws-cdk-lib/aws-ecs";
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const vpc = Vpc.fromLookup(this, 'Vpc', { vpcId: 'vpc-0316bba888afa43c5' });
        const cluster = Cluster.fromClusterAttributes(vpc, 'Cluster', { vpc, clusterName: 'default' });

        const task = new FargateTaskDefinition(this, 'TaskDefinition');
        const main = task.addContainer('main', {
            image: ContainerImage.fromRegistry('webapp:0.0.1-SNAPSHOT'),
        });
    }
}