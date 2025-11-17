# AI Prompt Application on AWS

This project is a full-stack AI-powered application. The backend is a Java Spring Boot application that connects to Amazon Bedrock, deployed as a containerized service on Amazon ECS with AWS Fargate. The frontend is a React application hosted on Amazon S3 and distributed globally via Amazon CloudFront. The entire infrastructure is managed as code using the AWS Cloud Development Kit (CDK).

## Architecture

- **Backend**: A Spring Boot application running in a Docker container, managed by ECS with Fargate. It takes user prompts and communicates with Amazon Bedrock to get AI-generated responses.
- **Frontend**: A React single-page application (SPA) hosted in an S3 bucket.
- **API**: An Application Load Balancer (ALB) routes traffic to the backend ECS service.
- **CDN**: Amazon CloudFront serves the frontend content from S3 for low-latency access.
- **AI Service**: Amazon Bedrock provides the generative AI capabilities.
- **Authentication**: Amazon Cognito manages user sign-up and sign-in.
- **Infrastructure as Code**: AWS CDK defines all cloud resources in TypeScript.

---

## Prerequisites

- **AWS CLI**: Configured with an active AWS account.
- **Node.js and npm**: For running the AWS CDK and the frontend application.
- **AWS CDK Toolkit**: `npm install -g aws-cdk`
- **Java 17+ & Maven**: For building the backend application.
- **Docker**: For building and publishing the backend container image.

---

## Deployment Guide

Follow these steps to deploy the entire application.

### 1. Deploy the Infrastructure with AWS CDK

First, navigate to the `cdk` directory to deploy the necessary AWS resources.

```bash
# Navigate to the cdk directory
cd cdk

# Install dependencies
npm install

# Bootstrap your AWS environment for CDK (only needs to be done once per region)
cdk bootstrap

# Deploy the stack
cdk deploy
```

This command will provision all the required resources. After it completes, **copy the outputs** from your terminal into a text file. You will need these values for the next steps.

### 2. Configure and Deploy the Frontend

Set up the React application to communicate with your new backend and authentication service.

```bash
# Navigate to the frontend directory
cd ../frontend

# Install dependencies
npm install
```

Next, create a configuration file `src/config.js` and populate it with the outputs from the CDK deployment.

**Create `frontend/src/config.js` with the following content:**

```javascript
// Replace the placeholder values with the actual outputs from your CDK deployment
export const cognitoConfig = {
    userPoolId: 'YOUR_USER_POOL_ID',
    userPoolClientId: 'YOUR_USER_POOL_CLIENT_ID',
    region: 'YOUR_AWS_REGION'
};
```

Now, build the frontend application and sync it to the S3 bucket.

```bash
# Build the React application
npm run build

# Sync the build directory to the S3 bucket
# Replace <FrontendBucketName> with the 'FrontendBucketName' output from the CDK deployment
aws s3 sync build/ s3://<FrontendBucketName>
```

### 3. Build and Deploy the Backend

Finally, build and push the container image for your Spring Boot application.

```bash
# Navigate to the backend directory
cd ../backend

# Build the Java application
mvn clean package

# Log Docker into your AWS ECR repository
# Replace <REGION> and <EcrRepositoryUri> with the outputs from the CDK deployment
aws ecr get-login-password --region <REGION> | docker login --username AWS --password-stdin <EcrRepositoryUri>

# Build the Docker image
docker build -t ai-prompt-backend .

# Tag the image for the ECR repository
docker tag ai-prompt-backend:latest <EcrRepositoryUri>:latest

# Push the image to ECR
docker push <EcrRepositoryUri>:latest
```

Once the image is pushed, the ECS service will automatically pull it and start the backend task. Your application is now fully deployed! You can access it via the `CloudFrontDistributionDomainName` from the CDK outputs.

---

## How to Update the Application

### Updating the Backend

If you change the backend Java code, you must rebuild and redeploy the container.

1.  **Build the application**: `cd backend && mvn clean package`
2.  **Build and push the new Docker image**: Follow step 3 from the deployment guide to build and push the new image to ECR.
3.  **Force a new deployment**: To make ECS pull the latest image, run the following command. You can get the cluster and service names from the CDK outputs or the AWS console.

    ```bash
    aws ecs update-service --cluster <CLUSTER_NAME> --service <SERVICE_NAME> --force-new-deployment
    ```

### Updating the Frontend

If you change the frontend React code, you must rebuild, redeploy to S3, and invalidate the CloudFront cache.

1.  **Build the application**: `cd frontend && npm run build`
2.  **Sync to S3**: `aws s3 sync build/ s3://<FrontendBucketName>`
3.  **Invalidate CloudFront Cache**: This ensures users get the latest version of your site.

    ```bash
    aws cloudfront create-invalidation --distribution-id <CloudFrontDistributionId> --paths "/*"
    ```
