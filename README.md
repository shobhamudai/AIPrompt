# AI Prompt Application on AWS

This project is a full-stack AI-powered application. The backend is a Java Spring Boot application that connects to Amazon Bedrock, deployed as a containerized service on Amazon ECS with AWS Fargate. The frontend is a React application hosted on Amazon S3 and distributed globally via Amazon CloudFront. The entire infrastructure is managed as code using the AWS Cloud Development Kit (CDK).

## Architecture

- **Backend**: A Spring Boot application running in a Docker container, managed by ECS with Fargate. It takes user prompts, communicates with Amazon Bedrock, and saves conversations to DynamoDB.
- **Frontend**: A React single-page application (SPA) hosted in an S3 bucket.
- **Database**: Amazon DynamoDB stores the chat history for each user.
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
- **Docker**: For building and publishing the backend container image. Docker must be running on your machine for deployment.

---

## Deployment Guide

This guide covers the first-time deployment for all parts of the application.

### Step 1: Configure the Frontend

Before deploying, you need to create a configuration file for the frontend.

1.  Navigate to the `frontend` directory: `cd frontend`
2.  Create a new file named `src/config.js`.
3.  Copy the following content into `src/config.js`. You will fill in the values after the first infrastructure deployment.

    ```javascript
    // Replace the placeholder values after the first CDK deployment
    export const cognitoConfig = {
        userPoolId: 'YOUR_USER_POOL_ID',
        userPoolClientId: 'YOUR_USER_POOL_CLIENT_ID',
        region: 'YOUR_AWS_REGION'
    };
    ```

### Step 2: Deploy the Backend and Infrastructure

There are two ways to deploy the backend: the automated method (recommended) and a manual alternative if you encounter network issues.

#### Option A: Automated Deployment (Recommended)

This single command builds the backend Java code, builds the Docker image, pushes it to AWS ECR, and deploys all the necessary AWS infrastructure (ECS, DynamoDB, Cognito, etc.).

1.  **Build the backend code:**
    ```bash
    # Navigate to the backend directory
    cd backend
    mvn clean package
    ```

2.  **Deploy with CDK:**
    ```bash
    # Navigate to the cdk directory
    cd ../cdk
    npm install
    cdk deploy
    ```
    The `cdk deploy` command will automatically find your packaged backend, build the Docker image, and publish it for you.

#### Option B: Manual Backend Deployment

Use this option if `cdk deploy` fails during the "publishing asset" phase due to network proxies or other connection issues.

1.  **Deploy the CDK stack first (without the backend):**
    This is necessary to create the ECR repository where you will push your image.
    ```bash
    # Navigate to the cdk directory
    cd cdk
    npm install
    # You may need to temporarily modify the CDK stack to use a placeholder image
    # to get the initial infrastructure deployed.
    cdk deploy 
    ```

2.  **Manually Build and Push the Docker Image:**
    ```bash
    # Navigate to the backend directory
    cd ../backend
    mvn clean package

    # Log Docker into your AWS ECR repository
    # Replace <REGION> and <EcrRepositoryUri> with the outputs from the CDK deployment
    aws ecr get-login-password --region <REGION> | docker login --username AWS --password-stdin <EcrRepositoryUri>

    # Build, tag, and push the image
    docker build -t ai-prompt-backend .
    docker tag ai-prompt-backend:latest <EcrRepositoryUri>:latest
    docker push <EcrRepositoryUri>:latest
    ```
3.  **Force a New Deployment in ECS:**
    After pushing the image, you must tell the ECS service to pull the new version.
    ```bash
    aws ecs update-service --cluster <CLUSTER_NAME> --service <SERVICE_NAME> --force-new-deployment
    ```

### Step 3: Finalize Frontend Configuration and Deployment

1.  **Update `frontend/src/config.js`:**
    After `cdk deploy` completes, it will print output values. Copy the `UserPoolId`, `UserPoolClientId`, and your AWS `region` into the `frontend/src/config.js` file you created earlier.

2.  **Build and Deploy the Frontend:**
    ```bash
    # Navigate to the frontend directory
    cd ../frontend
    npm install
    npm run build

    # Sync the build directory to the S3 bucket
    # Replace <FrontendBucketName> with the output from the CDK deployment
    aws s3 sync build/ s3://<FrontendBucketName>

    # Invalidate the CloudFront cache to ensure users get the latest version
    aws cloudfront create-invalidation --distribution-id <CloudFrontDistributionId> --paths "/*"
    ```

Your application is now fully deployed! Access it via the `CloudFrontDistributionDomainName` from the CDK outputs.
