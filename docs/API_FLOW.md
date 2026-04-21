# API Flow — AttendAI Cloud

## Overview

The system supports two face recognition paths:
- **Path A**: React → Spring Boot → AWS Services directly
- **Path B**: React → API Gateway → Lambda → AWS Services

---

## Authentication Flow

```
User fills Login form
      │
      ▼
React: signIn(email, password) [amazon-cognito-identity-js]
      │
      ▼
AWS Cognito User Pool
  ├─ Validates credentials
  └─ Returns: idToken (JWT), accessToken, refreshToken
      │
      ▼
React: stores idToken in localStorage
      │
      ▼
All subsequent API calls include:
  Authorization: Bearer <idToken>
```

---

## Path A — Spring Boot Direct

### 1. Register Student Face

```
POST /api/face/upload
Authorization: Bearer <JWT>
Body: { studentId, studentName, email, department, imageBase64 }

      │
      ▼
JwtAuthFilter.java
  └─ Validates JWT via Cognito JWKS
      │
      ▼
FaceController.java → S3Service.java
  └─ Upload image → s3://bucket/registered/<studentId>-<ts>.jpg
      │
      ▼
FaceController.java → RekognitionService.java
  └─ IndexFaces(collectionId, image, externalImageId=studentId)
  └─ Returns: faceId
      │
      ▼
Response: { success, faceId, s3Key }
```

### 2. Mark Attendance

```
POST /api/attendance/mark
Authorization: Bearer <JWT>
Body: { imageBase64 }

      │
      ▼
JwtAuthFilter.java → validates JWT
      │
      ▼
AttendanceController.java → S3Service.java
  └─ Upload → s3://bucket/attendance/attendance-<ts>.jpg
      │
      ▼
AttendanceController.java → RekognitionService.java
  └─ SearchFacesByImage(collectionId, image, threshold=90%)
  └─ Returns: [studentId, confidence] or empty
      │
      ├─ Match found → status = PRESENT
      └─ No match   → status = ABSENT
      │
      ▼
AttendanceController.java → DynamoDBService.java
  └─ PutItem({ studentId, timestamp, status, confidence, imageKey })
      │
      ▼
AttendanceController.java → SnsService.java
  └─ SNS Publish → email/SMS notification
      │
      ▼
Response: { studentId, confidence, status, timestamp, message }
```

### 3. Get Attendance Records

```
GET /api/attendance/list[?studentId=STU001]
Authorization: Bearer <JWT>

      │
      ▼
DynamoDBService.java
  ├─ with studentId  → DynamoDB Query (PK = studentId)
  └─ without         → DynamoDB Scan
      │
      ▼
Response: [ { studentId, studentName, timestamp, status, confidence } ]
```

---

## Path B — Lambda via API Gateway

```
POST /lambda/attendance/mark  (API Gateway endpoint)
Authorization: Bearer <JWT>    (Cognito Authorizer on API Gateway)
Body: { imageBase64 }

      │
      ▼
API Gateway
  └─ Cognito Authorizer validates JWT
      │
      ▼
Lambda: FaceRecognitionHandler.handleRequest()
  │
  ├─ S3Uploader.uploadImage()
  │   └─ s3://bucket/attendance/lambda-<ts>.jpg
  │
  ├─ RekognitionMatcher.searchFace()
  │   └─ SearchFacesByImage → [studentId, confidence]
  │
  └─ DynamoDBWriter.writeRecord()
      └─ PutItem to AttendanceRecords table
      │
      ▼
Response: { studentId, confidence, status, timestamp, imageKey }
```

---

## DynamoDB Schema

**Table:** `AttendanceRecords`

| Attribute   | Type   | Role                              |
|-------------|--------|-----------------------------------|
| studentId   | String | Partition Key (PK)                |
| timestamp   | String | Sort Key (SK) — ISO-8601          |
| status      | String | "PRESENT" or "ABSENT"             |
| confidence  | Number | Rekognition similarity score      |
| studentName | String | Display name                      |
| imageKey    | String | S3 object key of captured image   |
| date        | String | YYYY-MM-DD (for daily filtering)  |
| source      | String | "spring" or "lambda"              |

---

## S3 Bucket Structure

```
attendai-faces-bucket/
├── registered/              # Enrolled student face photos
│   ├── STU001-1713600000.jpg
│   └── STU002-1713600100.jpg
└── attendance/              # Daily attendance captures
    ├── attendance-2024-04-20T10-30-00Z.jpg
    └── lambda-2024-04-20T11-00-00Z.jpg
```

---

## JWT Token Flow Details

Cognito issues three tokens:
| Token         | Used for                                      | Expiry   |
|---------------|-----------------------------------------------|----------|
| ID Token      | Identity claims (email, name) — sent as Bearer| 1 hour   |
| Access Token  | Cognito user pool API access                  | 1 hour   |
| Refresh Token | Get new ID/Access tokens without re-login     | 30 days  |

Spring Boot validates the **ID Token** using:
1. Fetches JWKS from `https://cognito-idp.<region>.amazonaws.com/<pool-id>/.well-known/jwks.json`
2. Matches `kid` from token header to a public key
3. Verifies RS256 signature
4. Checks expiration
