#!/usr/bin/env node
import { App } from 'aws-cdk-lib';
import 'source-map-support/register';
import { AmplifyStack } from '../lib/AmplifyStack';

const app = new App();
new AmplifyStack(app, 'AmplifyStack', {
  env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: 'ap-northeast-1' }
});