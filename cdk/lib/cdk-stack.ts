import * as cdk from 'aws-cdk-lib';
import { Construct } from 'constructs';
import * as ecr from 'aws-cdk-lib/aws-ecr';
import * as ecs from 'aws-cdk-lib/aws-ecs';
import * as ecs_patterns from 'aws-cdk-lib/aws-ecs-patterns';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as cloudfront from 'aws-cdk-lib/aws-cloudfront';
import * as origins from 'aws-cdk-lib/aws-cloudfront-origins';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as cognito from 'aws-cdk-lib/aws-cognito';
import * as path from 'path';

export class AIPromptStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // --- USER AUTHENTICATION RESOURCES ---
    const userPool = new cognito.UserPool(this, 'AIPromptUserPool', {
      userPoolName: 'AIPromptUserPool',
      selfSignUpEnabled: true,
      accountRecovery: cognito.AccountRecovery.EMAIL_ONLY,
      userVerification: {
        emailStyle: cognito.VerificationEmailStyle.CODE,
      },
      autoVerify: { email: true },
      standardAttributes: {
        email: { required: true, mutable: false },
      },
    });

    const userPoolClient = new cognito.UserPoolClient(this, "AIPromptUserPoolClient", {
        userPool: userPool,
    });

    // --- BACKEND RESOURCES ---
    const repository = new ecr.Repository(this, 'AIPromptAppRepository', {
      repositoryName: 'ai-prompt-app-backend',
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      emptyOnDelete: true,
    });

    const cluster = new ecs.Cluster(this, 'AIPromptCluster', {});

    const fargateService = new ecs_patterns.ApplicationLoadBalancedFargateService(this, 'AIPromptFargateService', {
      cluster: cluster,
      cpu: 256,
      memoryLimitMiB: 512,
      desiredCount: 1,
      taskImageOptions: {
        // Reverted: Use a local asset. CDK will build and push this image.
        image: ecs.ContainerImage.fromAsset(path.resolve(__dirname, '../../backend')),
        containerPort: 8080,
        environment: {
          SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: `https://cognito-idp.${this.region}.amazonaws.com/${userPool.userPoolId}`,
        },
      },
      publicLoadBalancer: true,
      healthCheckGracePeriod: cdk.Duration.seconds(150),
    });

    // Add Bedrock permissions to the task role
    fargateService.taskDefinition.addToTaskRolePolicy(new iam.PolicyStatement({
      actions: ['bedrock:InvokeModel'],
      resources: ['*'], // For production, you might want to restrict this to specific model ARNs
    }));

    fargateService.targetGroup.configureHealthCheck({
      path: '/actuator/health',
      interval: cdk.Duration.seconds(30),
      healthyThresholdCount: 2,
      timeout: cdk.Duration.seconds(5),
    });

    // --- FRONTEND RESOURCES ---
    const websiteBucket = new s3.Bucket(this, 'AIPromptWebsiteBucket', {
      websiteIndexDocument: 'index.html',
      removalPolicy: cdk.RemovalPolicy.DESTROY,
      autoDeleteObjects: true,
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
    });

    const originAccessIdentity = new cloudfront.OriginAccessIdentity(this, 'AIPromptOAI');
    websiteBucket.grantRead(originAccessIdentity);

    const distribution = new cloudfront.CloudFrontWebDistribution(this, 'AIPromptDistribution', {
      originConfigs: [
        {
          s3OriginSource: {
            s3BucketSource: websiteBucket,
            originAccessIdentity: originAccessIdentity,
          },
          behaviors: [
            {
              isDefaultBehavior: true,
              viewerProtocolPolicy: cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
            },
          ],
        },
        {
          customOriginSource: {
            domainName: fargateService.loadBalancer.loadBalancerDnsName,
            originProtocolPolicy: cloudfront.OriginProtocolPolicy.HTTP_ONLY,
          },
          behaviors: [
            {
              pathPattern: '/api/*',
              viewerProtocolPolicy: cloudfront.ViewerProtocolPolicy.REDIRECT_TO_HTTPS,
              allowedMethods: cloudfront.CloudFrontAllowedMethods.ALL,
              cachedMethods: cloudfront.CloudFrontAllowedCachedMethods.GET_HEAD_OPTIONS,
              forwardedValues: {
                queryString: true,
                cookies: { forward: 'all' },
                headers: ['*'],
              },
              defaultTtl: cdk.Duration.seconds(0),
            },
          ],
        },
      ],
    });

    // --- STACK OUTPUTS ---
    new cdk.CfnOutput(this, 'LoadBalancerDNS', { value: fargateService.loadBalancer.loadBalancerDnsName });
    new cdk.CfnOutput(this, 'EcrRepositoryUri', { value: repository.repositoryUri });
    new cdk.CfnOutput(this, 'FrontendBucketName', { value: websiteBucket.bucketName });
    new cdk.CfnOutput(this, 'CloudFrontDistributionId', { value: distribution.distributionId });
    new cdk.CfnOutput(this, 'CloudFrontDistributionDomainName', { value: distribution.distributionDomainName });
    new cdk.CfnOutput(this, "UserPoolId", { value: userPool.userPoolId });
    new cdk.CfnOutput(this, "UserPoolClientId", { value: userPoolClient.userPoolClientId });
  }
}
