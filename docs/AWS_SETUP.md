# AWS Setup Guide — AttendAI Cloud

Complete step-by-step guide to configure all AWS services required for AttendAI Cloud.

---

## Prerequisites
- AWS Account with admin permissions
- AWS CLI installed and configured (`aws configure`)
- Java 17 + Maven installed
- Node.js 18+ installed

---

## Step 1 — AWS Cognito (User Authentication)

### 1.1 Create User Pool
```bash
aws cognito-idp create-user-pool \
  --pool-name "AttendAIUserPool" \
  --policies '{"PasswordPolicy":{"MinimumLength":8,"RequireUppercase":true,"RequireLowercase":true,"RequireNumbers":true}}' \
  --auto-verified-attributes email \
  --username-attributes email \
  --region us-east-1
```
**Save the returned `UserPoolId`** (e.g., `us-east-1_AbcDef123`)

### 1.2 Create App Client
```bash
aws cognito-idp create-user-pool-client \
  --user-pool-id <YOUR_USER_POOL_ID> \
  --client-name "AttendAIWebClient" \
  --no-generate-secret \
  --explicit-auth-flows ALLOW_USER_PASSWORD_AUTH ALLOW_REFRESH_TOKEN_AUTH \
  --region us-east-1
```
**Save the returned `ClientId`**

### 1.3 Enable USER_PASSWORD_AUTH
In AWS Console → Cognito → Your User Pool → App clients → Edit → Enable `ALLOW_USER_PASSWORD_AUTH`

---

## Step 2 — Amazon S3 (Image Storage)

### 2.1 Create Bucket
```bash
aws s3api create-bucket \
  --bucket attendai-faces-bucket \
  --region us-east-1
```

### 2.2 Block Public Access (recommended)
```bash
aws s3api put-public-access-block \
  --bucket attendai-faces-bucket \
  --public-access-block-configuration \
    BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true
```

### 2.3 Create Folder Structure
```bash
# Create placeholder objects to simulate folders
echo "" | aws s3 cp - s3://attendai-faces-bucket/registered/.keep
echo "" | aws s3 cp - s3://attendai-faces-bucket/attendance/.keep
```

---

## Step 3 — Amazon Rekognition (Face Recognition)

### 3.1 Create Face Collection
```bash
aws rekognition create-collection \
  --collection-id "attendai-faces" \
  --region us-east-1
```

> **Note**: You can also call `POST /api/face/collection/create` from the Spring Boot API after starting the backend.

### 3.2 Verify Collection
```bash
aws rekognition describe-collection \
  --collection-id "attendai-faces" \
  --region us-east-1
```

---

## Step 4 — Amazon DynamoDB (Attendance Records)

### 4.1 Create Table
```bash
aws dynamodb create-table \
  --table-name AttendanceRecords \
  --attribute-definitions \
    AttributeName=studentId,AttributeType=S \
    AttributeName=timestamp,AttributeType=S \
  --key-schema \
    AttributeName=studentId,KeyType=HASH \
    AttributeName=timestamp,KeyType=RANGE \
  --billing-mode PAY_PER_REQUEST \
  --region us-east-1
```

### 4.2 Verify Table
```bash
aws dynamodb describe-table --table-name AttendanceRecords --region us-east-1
```

---

## Step 5 — Amazon SNS (Notifications)

### 5.1 Create Topic
```bash
aws sns create-topic \
  --name AttendanceNotifications \
  --region us-east-1
```
**Save the returned `TopicArn`** (e.g., `arn:aws:sns:us-east-1:123456789:AttendanceNotifications`)

### 5.2 Subscribe an Email
```bash
aws sns subscribe \
  --topic-arn <YOUR_TOPIC_ARN> \
  --protocol email \
  --notification-endpoint your@email.com \
  --region us-east-1
```
Check your email inbox and confirm the subscription.

---

## Step 6 — IAM Role & Permissions

### 6.1 Create IAM Policy
Create file `attendai-policy.json`:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject", "s3:GetObject", "s3:DeleteObject", "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::attendai-faces-bucket",
        "arn:aws:s3:::attendai-faces-bucket/*"
      ]
    },
    {
      "Effect": "Allow",
      "Action": [
        "rekognition:IndexFaces",
        "rekognition:SearchFacesByImage",
        "rekognition:CreateCollection",
        "rekognition:DescribeCollection",
        "rekognition:ListFaces",
        "rekognition:DeleteFaces"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem",
        "dynamodb:GetItem",
        "dynamodb:Query",
        "dynamodb:Scan",
        "dynamodb:UpdateItem"
      ],
      "Resource": "arn:aws:dynamodb:us-east-1:*:table/AttendanceRecords"
    },
    {
      "Effect": "Allow",
      "Action": ["sns:Publish"],
      "Resource": "<YOUR_TOPIC_ARN>"
    },
    {
      "Effect": "Allow",
      "Action": [
        "cognito-idp:InitiateAuth",
        "cognito-idp:GetUser"
      ],
      "Resource": "<YOUR_USER_POOL_ARN>"
    }
  ]
}
```

```bash
aws iam create-policy \
  --policy-name AttendAIPolicy \
  --policy-document file://attendai-policy.json
```

### 6.2 For Local Development
Configure AWS credentials in `~/.aws/credentials`:
```ini
[default]
aws_access_key_id = YOUR_ACCESS_KEY_ID
aws_secret_access_key = YOUR_SECRET_ACCESS_KEY
region = us-east-1
```

---

## Step 7 — AWS Lambda (Serverless Option)

### 7.1 Build Lambda JAR
```bash
cd lambda
mvn clean package -DskipTests
# Output: target/lambda-face-recognition-1.0.0.jar
```

### 7.2 Create Lambda Execution Role
```bash
aws iam create-role \
  --role-name AttendAILambdaRole \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": {"Service": "lambda.amazonaws.com"},
      "Action": "sts:AssumeRole"
    }]
  }'

# Attach policies
aws iam attach-role-policy \
  --role-name AttendAILambdaRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole

aws iam attach-role-policy \
  --role-name AttendAILambdaRole \
  --policy-arn <YOUR_ATTENDAI_POLICY_ARN>
```

### 7.3 Deploy Lambda Function
```bash
aws lambda create-function \
  --function-name attendai-face-recognition \
  --runtime java17 \
  --role arn:aws:iam::<ACCOUNT_ID>:role/AttendAILambdaRole \
  --handler com.attendai.lambda.FaceRecognitionHandler::handleRequest \
  --zip-file fileb://lambda/target/lambda-face-recognition-1.0.0.jar \
  --timeout 30 \
  --memory-size 512 \
  --environment Variables="{
    AWS_REGION=us-east-1,
    S3_BUCKET_NAME=attendai-faces-bucket,
    REKOGNITION_COLLECTION_ID=attendai-faces,
    REKOGNITION_CONFIDENCE_THRESHOLD=90.0,
    DYNAMODB_TABLE_NAME=AttendanceRecords
  }" \
  --region us-east-1
```

### 7.4 Create API Gateway
```bash
# Create REST API
aws apigateway create-rest-api --name "AttendAI-API" --region us-east-1

# (Then configure resources/methods/Cognito Authorizer via AWS Console)
```

---

## Step 8 — CloudWatch (Monitoring)

CloudWatch logs are created automatically:
- Lambda: `/aws/lambda/attendai-face-recognition`
- Spring Boot: Configure log4j2/logback to use CloudWatch Logs Agent or run on ECS/EC2

### Create Log Group for Spring Boot (optional)
```bash
aws logs create-log-group \
  --log-group-name /attendai/spring-boot \
  --region us-east-1
```

---

## Environment Variables Summary

### Backend (`backend/.env` or system environment)
```env
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=<your-key>
AWS_SECRET_ACCESS_KEY=<your-secret>
S3_BUCKET_NAME=attendai-faces-bucket
REKOGNITION_COLLECTION_ID=attendai-faces
REKOGNITION_CONFIDENCE_THRESHOLD=90.0
DYNAMODB_TABLE_NAME=AttendanceRecords
SNS_TOPIC_ARN=arn:aws:sns:us-east-1:<account>:AttendanceNotifications
COGNITO_USER_POOL_ID=us-east-1_xxxxxxxxx
COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxx
```

### Frontend (`frontend/.env`)
```env
VITE_AWS_REGION=us-east-1
VITE_COGNITO_USER_POOL_ID=us-east-1_xxxxxxxxx
VITE_COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxx
VITE_API_BASE_URL=http://localhost:8080/api
```
