# Testing FinPilot Authentication Flow

This guide describes how to execute automated tests and manual end-to-end tests for the Phase 4 authentication milestone.

---

## 1. Automated Backend Unit Tests

Run all unit tests covering `UserService`, `JwtService`, and `AuthController`:

```powershell
cd Backend
.\mvnw.cmd test "-Dtest=JwtServiceTest,UserServiceTest,AuthControllerTest"
```

### Verified Test Cases:
- `UserServiceTest.testProcessOAuthUser_NewUser`: Verifies new Google user creation in PostgreSQL.
- `UserServiceTest.testProcessOAuthUser_ExistingUser`: Verifies existing Google user lookup and safe profile field updates without duplicate rows.
- `UserServiceTest.testProcessOAuthUser_MissingEmail`: Verifies rejection when email is missing.
- `JwtServiceTest.testGenerateAndExtractClaims`: Verifies JWT generation, subject extraction, and claim resolution.
- `JwtServiceTest.testInvalidToken`: Verifies invalid token rejection.
- `AuthControllerTest.testGetCurrentUser_Authenticated`: Verifies `GET /api/auth/me` returns 200 OK + profile details for authenticated requests.
- `AuthControllerTest.testGetCurrentUser_Unauthenticated`: Verifies 401 Unauthorized for unauthenticated requests.
- `AuthControllerTest.testGetProtectedTestEndpoint_Authenticated`: Verifies 200 OK for protected endpoints.
- `AuthControllerTest.testGetProtectedTestEndpoint_Unauthenticated`: Verifies 401 Unauthorized for unauthenticated requests.

---

## 2. End-to-End Test Steps (Manual Flow)

### Prerequisites:
1. PostgreSQL running on `localhost:5432` with database `finance_db`.
2. Real Google OAuth Client ID & Secret set in `.env` or environment variables:
   ```powershell
   $env:GOOGLE_CLIENT_ID="your-client-id"
   $env:GOOGLE_CLIENT_SECRET="your-client-secret"
   ```

### Step 1: Start Backend
```powershell
cd Backend
.\mvnw.cmd spring-boot:run
```

### Step 2: Start Frontend
```powershell
cd frontend
npm run dev
```

### Step 3: Execute 10 Test Cases

| Test # | Action | Expected Result |
|---|---|---|
| **TEST 1** | Open `http://localhost:8080/oauth2/authorization/google` or click **Continue with Google** on `http://localhost:5173/login`. | Redirects to Google login screen. |
| **TEST 2** | Authenticate with Google account. | Google redirects back to Spring Security OAuth2 callback `/login/oauth2/code/google`. |
| **TEST 3** | Check PostgreSQL database: `SELECT * FROM users;`. | One user row exists with `auth_provider = 'GOOGLE'`, provider ID, email, name. |
| **TEST 4** | Log out, then log in with Google again. | No duplicate row created in `users` table; `updated_at` is updated. |
| **TEST 5** | Call `GET /api/auth/me` with `Authorization: Bearer <JWT>`. | Returns `200 OK` with user details object. |
| **TEST 6** | Call `GET /api/test/protected` with valid JWT. | Returns `200 OK` with message: `"You have access to a protected endpoint"`. |
| **TEST 7** | Call `GET /api/test/protected` without `Authorization` header. | Returns `401 Unauthorized`. |
| **TEST 8** | Call `GET /api/test/protected` with forged/invalid JWT. | Returns `401 Unauthorized`. |
| **TEST 9** | Refresh browser on `/dashboard`. | Auth state is restored cleanly via token in `localStorage` and `/api/auth/me`. |
| **TEST 10** | Click **Logout**. | Token cleared from client state, redirected to `/login`, direct access to `/dashboard` blocked. |
