# AttendAI Cloud — Face Recognition Based Attendance System

> A production-grade, cloud-native attendance management system using face recognition powered by AWS Rekognition.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React.js (Vite), react-webcam, Amazon Cognito JS |
| Backend | Spring Boot 3, Spring Security, AWS SDK v2 |
| Serverless | AWS Lambda (Java 17), API Gateway |
| Auth | AWS Cognito (JWT / RS256) |
| Storage | Amazon S3 |
| Face AI | Amazon Rekognition |
| Database | Amazon DynamoDB |
| Notifications | Amazon SNS (Email) |
| Monitoring | Amazon CloudWatch |

---

## Project Structure

```
cloudProject/
├── frontend/          # React.js application (Vite)
├── backend/           # Spring Boot REST API
├── lambda/            # AWS Lambda function (Java)
├── docs/
│   ├── API_FLOW.md   # Detailed API flow documentation
│   └── AWS_SETUP.md  # AWS configuration step-by-step
└── README.md
```

---

## Quick Start — Local Development

### Step 1: Clone and Setup Environment Variables

**Frontend:**
```bash
cd frontend
cp .env.example .env
# Edit .env with your Cognito User Pool ID and Client ID
```

**Backend:**
```bash
# Set environment variables (Windows PowerShell)
$env:AWS_REGION = "us-east-1"
$env:AWS_ACCESS_KEY_ID = "YOUR_KEY"
$env:AWS_SECRET_ACCESS_KEY = "YOUR_SECRET"
$env:S3_BUCKET_NAME = "attendai-faces-bucket"
$env:REKOGNITION_COLLECTION_ID = "attendai-faces"
$env:REKOGNITION_CONFIDENCE_THRESHOLD = "90.0"
$env:DYNAMODB_TABLE_NAME = "AttendanceRecords"
$env:SNS_TOPIC_ARN = "arn:aws:sns:us-east-1:..."
$env:COGNITO_USER_POOL_ID = "us-east-1_xxxxxxx"
$env:COGNITO_CLIENT_ID = "xxxxxxxxxxxxxxxxx"
```

### Step 2: Run Frontend
```bash
cd frontend
npm install
npm run dev
# App runs at http://localhost:5173
```

### Step 3: Run Backend
```bash
cd backend
mvn clean spring-boot:run
# API runs at http://localhost:8080
```

### Step 4: Build Lambda (Optional)
```bash
cd lambda
mvn clean package -DskipTests
# JAR: target/lambda-face-recognition-1.0.0.jar
```

---

## AWS Setup

See [docs/AWS_SETUP.md](docs/AWS_SETUP.md) for complete AWS configuration including:
- Cognito User Pool + App Client
- S3 Bucket with folder structure
- Rekognition Face Collection
- DynamoDB Table
- SNS Topic + Email subscription
- Lambda deployment
- API Gateway setup
- IAM Roles and Policies

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/login` | ❌ | Authenticate, get JWT |
| POST | `/api/face/upload` | ✅ JWT | Register student face |
| POST | `/api/face/collection/create` | ✅ JWT | Create Rekognition collection |
| POST | `/api/attendance/mark` | ✅ JWT | Mark attendance via face |
| GET | `/api/attendance/list` | ✅ JWT | Get attendance records |
| GET | `/api/attendance/summary` | ✅ JWT | Today's stats |

See [docs/API_FLOW.md](docs/API_FLOW.md) for detailed request/response documentation.

---

## Features

- 🔐 **Secure Authentication** — AWS Cognito with JWT RS256 validation
- 📸 **Real-time Camera** — Browser webcam capture with face guide overlay
- 🧠 **AI Face Recognition** — AWS Rekognition with configurable confidence threshold
- 📊 **Live Dashboard** — Stats cards + searchable attendance table
- 👤 **Student Registration** — Register faces with student info
- 📧 **Email Notifications** — SNS alerts when attendance is marked
- ☁️ **Fully Serverless Option** — Lambda + API Gateway for zero-server deployment
- 📈 **CloudWatch Monitoring** — Logs and error tracking for all services

---

## Default Credentials (Local Testing)

Create a test user in Cognito:
```bash
aws cognito-idp admin-create-user \
  --user-pool-id <YOUR_USER_POOL_ID> \
  --username admin@attendai.com \
  --temporary-password Admin@123 \
  --user-attributes Name=email,Value=admin@attendai.com Name=email_verified,Value=true

aws cognito-idp admin-set-user-password \
  --user-pool-id <YOUR_USER_POOL_ID> \
  --username admin@attendai.com \
  --password Admin@1234 \
  --permanent
```

---

## License

MIT — Free to use for academic and educational purposes.

---

*Built for Final Year Engineering Project — AttendAI Cloud © 2024*
