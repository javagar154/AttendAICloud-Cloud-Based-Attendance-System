# AttendAI Cloud — Build Tracker

## Phase 1: Project Scaffolding
- [x] Implementation plan created
- [x] Create folder structure
- [x] Initialize frontend (Vite + React)
- [x] Initialize backend (Spring Boot)
- [x] Initialize Lambda

## Phase 2: Frontend
- [x] index.html, index.css, App.jsx, App.css
- [x] awsConfig.js, authService.js, apiService.js, attendanceService.js
- [x] Login.jsx, ProtectedRoute.jsx
- [x] Dashboard.jsx, AttendanceTable.jsx, StatsCards.jsx
- [x] CameraCapture.jsx, FacePreview.jsx
- [x] Navbar.jsx, Sidebar.jsx
- [x] RegisterFace.jsx (student face registration UI)
- [x] package.json, .env.example

## Phase 3: Backend (Spring Boot)
- [x] pom.xml
- [x] application.yml
- [x] AttendAiApplication.java
- [x] SecurityConfig.java, CognitoConfig.java, AwsConfig.java
- [x] JwtAuthFilter.java, JwtTokenUtil.java
- [x] AuthController.java, FaceController.java, AttendanceController.java
- [x] CognitoAuthService.java, S3Service.java, RekognitionService.java
- [x] DynamoDBService.java, SnsService.java
- [x] Models: AttendanceRecord, FaceUploadRequest, ApiResponse
- [x] GlobalExceptionHandler.java

## Phase 4: Lambda
- [x] pom.xml
- [x] FaceRecognitionHandler.java
- [x] S3Uploader.java, RekognitionMatcher.java, DynamoDBWriter.java

## Phase 5: Documentation
- [x] README.md
- [x] docs/API_FLOW.md
- [x] docs/AWS_SETUP.md

## Phase 6: Verification
- [x] Verify frontend builds
- [/] Verify backend compiles (Maven not found locally)
- [/] Verify lambda compiles (Maven not found locally)
- [x] Create walkthrough
