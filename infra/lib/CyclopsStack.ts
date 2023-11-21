import { App as AmplifyApp, GitHubSourceCodeProvider } from '@aws-cdk/aws-amplify-alpha';
import { Stack, StackProps } from "aws-cdk-lib";
import { BuildSpec } from 'aws-cdk-lib/aws-codebuild';
import { Secret } from 'aws-cdk-lib/aws-secretsmanager';
import { Construct } from "constructs";

export class CyclopsStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const amplifyApp = new AmplifyApp(this, 'AmplifyApp', {
            sourceCodeProvider: new GitHubSourceCodeProvider({
                owner: 'mahitotsu',
                repository: 'cyclops',
                oauthToken: Secret.fromSecretNameV2(this, 'GithubSecret', 'github/mahitotsu').secretValueFromJson('personal_access_token'),
            }),
            autoBranchDeletion: true,
            buildSpec: BuildSpec.fromObjectToYaml({
                version: '1.0',
                frontend: {
                    buildPath: '/front',
                    phases: {
                        preBuild: {
                            commands: [
                                'npm i'
                            ]
                        },
                        build: {
                            commands: [
                                'npm run build'
                            ]
                        },
                        artifacts: {
                            baseDirectory: '.amplify-hosting',
                            files: ['**/*'],
                        },
                        cache: {
                            paths: ['node_modules/**/*'],
                        },
                    },
                }
            }),
        });
    }
}