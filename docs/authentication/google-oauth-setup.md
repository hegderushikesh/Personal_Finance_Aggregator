# Google OAuth 2.0 Setup Guide for FinPilot

This document explains how to set up Google OAuth 2.0 credentials in the Google Cloud Console for local development and production.

---

## 1. Create a Google Cloud Project

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Log in with your Google account.
3. Click the project dropdown at the top navigation bar and select **New Project**.
4. Enter `FinPilot` as the Project Name.
5. Click **Create**.

---

## 2. Configure OAuth Consent Screen

1. In the left navigation menu, go to **APIs & Services > OAuth consent screen**.
2. Select **External** (or **Internal** if using Google Workspace) and click **Create**.
3. Fill in required App Information:
   - **App name**: `FinPilot`
   - **User support email**: Select your email.
   - **Developer contact information**: Enter your email.
4. Click **Save and Continue**.
5. Under **Scopes**, click **Add or Remove Scopes** and select:
   - `openid`
   - `.../auth/userinfo.email`
   - `.../auth/userinfo.profile`
6. Click **Update**, then **Save and Continue**.
7. Under **Test Users**, add your Google email address for testing.
8. Click **Save and Continue**.

---

## 3. Create OAuth Client Credentials

1. Go to **APIs & Services > Credentials**.
2. Click **+ Create Credentials** at the top and select **OAuth client ID**.
3. Set **Application type** to: `Web application`.
4. Set **Name** to: `FinPilot Web Client`.
5. Under **Authorized JavaScript origins**, add:
   - `http://localhost:8080`
   - `http://localhost:5173`
6. Under **Authorized redirect URIs**, add the exact Spring Security OAuth2 callback URI:
   - `http://localhost:8080/login/oauth2/code/google`
7. Click **Create**.
8. A modal will display your **Client ID** and **Client Secret**. Copy both values.

---

## 4. Set Environment Variables

Create a `.env` file in the root workspace directory (or export variables in your shell) with the copied credentials:

```bash
GOOGLE_CLIENT_ID=your-actual-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-actual-client-secret
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=86400000
DATABASE_URL=jdbc:postgresql://localhost:5432/finance_db
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=rushi3717
FRONTEND_URL=http://localhost:5173
```

> **IMPORTANT**: Never commit `.env` or real client secrets to git repository. `.env` is listed in `.gitignore`.

---

## 5. How to Start Spring Boot

Ensure PostgreSQL is running and database `finance_db` exists, then launch Spring Boot with your environment variables:

```powershell
$env:GOOGLE_CLIENT_ID="your-client-id"
$env:GOOGLE_CLIENT_SECRET="your-client-secret"
$env:JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
.\mvnw.cmd spring-boot:run
```

---

## 6. How to Test Authorization Flow

1. Open your browser and navigate to:
   `http://localhost:8080/oauth2/authorization/google`
2. You will be redirected to Google's consent screen.
3. Authenticate with your Google account.
4. Upon successful login, Spring Boot will process your user details, save/update the user in PostgreSQL, generate an application JWT, and redirect you to the React application at `http://localhost:5173`.
