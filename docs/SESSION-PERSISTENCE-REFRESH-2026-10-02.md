# Session Persistence / Refresh Analysis — 2026-10-02

## Objective

Determine why the reference Smart OSM+ application can be reopened without asking for the username every time, and how the authenticated state survives app restarts.

## Key finding

The reference application contains explicit token-refresh logic.

Observed static strings include:

- `[AuthContext] checkAuthStatus error:`
- `[AuthContext] refreshUser error:`
- `[AuthContext] updateUser error:`
- `[AuthContext] No user sub found, cannot refresh`
- `[Auth] Login response did NOT include refresh_token. Token refresh will NOT work - user will need to re-login when token expires.`
- `No refresh token stored. Session may not support refresh.`
- `No access_token in refresh response`
- `access_token`
- `refresh_token`
- `expires_in`

This is strong evidence that the reference app is designed around a renewable authenticated session rather than forcing a full login on every launch.

## Secure persistence

The APK configuration explicitly includes the Expo SecureStore plugin:

`expo-secure-store`

The bundle also contains SecureStore error/log strings and a token-storage helper message:

- `[getAuthToken] Error retrieving token:`
- `[getAuthToken] No token found in storage`

Therefore the working model is:

    login
      ↓
    access token + refresh token
      ↓
    secure local storage
      ↓
    app restart
      ↓
    AuthContext.checkAuthStatus()
      ↓
    restore user/session
      ↓
    refresh access token when required
      ↓
    continue using API

This explains how the user can reopen the application without entering the username again.

## Refresh endpoint

The reference APK contains this production endpoint:

`https://gw1.hss.moph.go.th/api/exchange/auth/token/refreshAccessToken`

This is separate from the Smart OSM data API host.

The exact request body, authentication method, refresh-token rotation behavior, and response lifetime remain unverified.

## Separate HSS exchange-token evidence

The bundle also contains:

- `getHSSExchangeToken`
- `HSS Exchange token initialization error:`
- `HSS Exchange token not found. Please call getHSSExchangeToken() first.`
- `HSS Exchange token timeout of HSS token not found or invalid for current user.`
- `hss_exchange_token_expiry`
- `HSS token request failed`
- `/api/exchange/...` routes

This indicates that there may be an additional HSS exchange-token layer in addition to the normal application access/refresh token pair.

Do not collapse these tokens into one token until the runtime request sequence proves they are the same.

## Runtime AdGuard correlation

The supplied AdGuard activity log contains a real reference-app sequence:

1. The browser reached the ThaiPHC callback endpoint after the ThaiD flow:
   `api-thaiphc.hss.moph.go.th/callback`
2. The callback state identifies the user type as `osm`.
3. Approximately one minute later, the Smart OSM application itself opened a TLS connection to:
   `api-thaiphc.hss.moph.go.th`
4. AdGuard records the application request as a successful TLS tunnel.

Because AdGuard records only the CONNECT/TLS tunnel for this native request, the exact HTTPS path is not visible in this export.

Sensitive callback values such as authorization codes, state values, and nonce values are intentionally excluded from this document.

## Working session model

The current best-supported model is:

    ThaiD
      ↓
    callback / identity result
      ↓
    HSS / Smart OSM exchange
      ↓
    application access token
      +
    refresh token / renewable session
      ↓
    SecureStore
      ↓
    AuthContext.checkAuthStatus()
      ↓
    refresh when necessary
      ↓
    authenticated API

Possible additional layer:

    HSS exchange token
      ↓
    expiry tracked by hss_exchange_token_expiry
      ↓
    renewed/reacquired when required

## Why the session appears not to expire

The most likely explanation, supported by the static refresh logic, is:

**The access token can expire, but the application silently renews the authenticated session using a persisted refresh credential.**

Therefore:

    access token expiry
          ≠
    user session expiry

A user only needs to perform the full login again when the renewable session/refresh mechanism is no longer valid, missing, revoked, or rejected by the backend.

The exact lifetime of each token is still unverified.

## What must be measured next

Do not guess token lifetimes. Measure the reference application with metadata only.

### Test A — app restart

1. Authenticate normally.
2. Close the app.
3. Reopen.
4. Record whether the app performs:
   - token read;
   - user/profile request;
   - refresh request;
   - direct resource request.

### Test B — long idle period

1. Authenticate.
2. Leave the app unused.
3. Reopen after a known interval.
4. Observe whether a refresh request occurs.

### Test C — force-stop

1. Authenticate.
2. Android force-stop the app.
3. Reopen.
4. Compare with Test A.

### Test D — network unavailable

1. Authenticate.
2. Disable network.
3. Reopen.
4. Observe whether the app:
   - opens local shell;
   - reports session state;
   - waits for network;
   - requests login.

### Test E — refresh boundary

With an authorized test account and only metadata capture, determine the point at which:
- access token is refreshed;
- HSS exchange token is reacquired;
- full login is required.

## Security boundary

Never capture or store:
- access tokens;
- refresh tokens;
- authorization codes;
- cookies;
- passwords;
- PINs;
- citizen IDs;
- health data.

TLS/CA verification must remain enabled.

## Engineering consequence for Smart_Osm22

Do not implement:

    every app launch → ask username/password

Instead implement the architecture around:

    AuthSession
      ├── accessToken
      ├── refreshToken (secure storage only)
      ├── expiresAt
      ├── user identity
      └── refresh/re-auth state

and keep the exact backend contract behind an authentication adapter until runtime verification is complete.

PIN and biometric mechanics remain outside this analysis.

## Evidence status

| Finding | Status |
|---|---|
| Reference app has access-token handling | VERIFIED |
| Reference app has refresh-token handling | VERIFIED |
| Reference app has automatic refresh logic | STRONGLY INDICATED |
| SecureStore is included in app configuration | VERIFIED |
| Token retrieval from persistent storage exists | STRONGLY INDICATED |
| Production refresh endpoint exists | VERIFIED |
| HSS exchange-token layer exists | STRONGLY INDICATED |
| HSS exchange-token expiry is tracked | VERIFIED from bundle string |
| Exact access-token lifetime | UNVERIFIED |
| Exact refresh-token lifetime | UNVERIFIED |
| Exact refresh cadence | UNVERIFIED |
| Exact HSS exchange-token lifetime | UNVERIFIED |
| Exact logout/revocation behavior | UNVERIFIED |

This is a reverse-engineering analysis document only. It does not change production authentication behavior.
