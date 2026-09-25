# FinPilot Authentication Architecture

This document describes the complete Phase 4 end-to-end authentication architecture for FinPilot.

---

## Architecture Overview

```
React (http://localhost:5173)
  │
  │ Click "Continue with Google"
  ▼
Spring Boot (http://localhost:8080/oauth2/authorization/google)
  │
  ▼
Google OAuth 2.0 Provider
  │
  │ User authenticates & approves scopes (openid, profile, email)
  ▼
Spring Security OAuth2 Callback (/login/oauth2/code/google)
  │
  ▼
OAuth2LoginSuccessHandler
  │
  ▼
UserService.processOAuthUser(OAuth2User)
  │  ├── Search user by authProvider (GOOGLE) & providerId (sub)
  │  ├── Search user by email
  │  └── Create or Update User in PostgreSQL (`users` table)
  ▼
JwtService.generateToken(User)
  │
  ▼
Redirect to React Frontend (http://localhost:5173/auth/callback?token=...)
  │
  ▼
React AuthCallback Page
  │  ├── Store token in localStorage (`finpilot_token`)
  │  └── Dispatch Redux `fetchCurrentUser()` -> GET /api/auth/me
  ▼
Protected Dashboard & REST API Interactions (Authorization: Bearer <JWT>)
```

---

## Detailed Components

### 1. Spring Security & OAuth 2.0 (`SecurityConfig.java`)
- **OAuth Authorization Base URI**: `/oauth2/authorization`
- **OAuth Callback URI**: `/login/oauth2/code/google`
- **Public Endpoints**: `/oauth2/**`, `/login/**`, `/error`
- **Protected Endpoints**: `/api/auth/me`, `/api/test/protected`, and all future financial resource endpoints.

### 2. User Persistence (`User.java`, `UserService.java`, `UserRepository.java`)
- **Table Name**: `users`
- **Fields**: `id`, `name`, `email`, `password` (nullable for OAuth), `authProvider` (`GOOGLE` / `LOCAL`), `providerId` (Google `sub`), `profileImageUrl`, `createdAt`, `updatedAt`.
- **Idempotent User Lookup**: Prevents duplicate account creation when users log in repeatedly with the same Google credentials.

### 3. Application JWT (`JwtService.java`)
- **Library**: `io.jsonwebtoken` (JJWT 0.12.6).
- **Signing Algorithm**: HMAC SHA-256 with strong secret (`JWT_SECRET`).
- **Claims Included**:
  - `sub`: Application User ID
  - `email`: User Email
  - `role`: `ROLE_USER`
  - `iat` / `exp`: Issued at & Expiration timestamp.
- **Security Rule**: Sensitive financial data, secrets, or raw passwords are **never** included inside the JWT payload.

### 4. JWT Authentication Filter (`JwtAuthenticationFilter.java`)
- Extends `OncePerRequestFilter`.
- Intercepts requests with `Authorization: Bearer <JWT>` header.
- Validates token validity & expiration, extracts User ID, loads `UserPrincipal`, and populates Spring `SecurityContextHolder`.

### 5. React Integration (`authSlice.js`, `useAuth.js`, `api.js`)
- **Axios Client**: Centralized request/response interceptor attaching Bearer tokens automatically and handling 401 Unauthorized responses cleanly.
- **Redux Store**: Maintains global auth state (`user`, `token`, `isAuthenticated`, `initialized`, `loading`).
- **Protected Routes**: `ProtectedRoute.jsx` prevents unauthenticated navigation to protected pages.
- **Logout**: Clears client authentication token and Redux state, returning user to `/login`.
