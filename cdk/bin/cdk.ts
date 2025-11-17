#!/usr/bin/env node
import * as cdk from 'aws-cdk-lib';
import { AIPromptStack } from '../lib/cdk-stack';

const app = new cdk.App();

new AIPromptStack(app, 'AIPromptStack', {
  /* 
   * It's a good practice to specify the environment for your stacks.
   * This makes them region and account-specific.
   */
  // env: { account: process.env.CDK_DEFAULT_ACCOUNT, region: process.env.CDK_DEFAULT_REGION },
});
