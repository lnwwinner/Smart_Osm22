# Session Persistence — Static Evidence V2 — 2026-10-02

## New static evidence from the reference APK

Further inspection of the Hermes bundle confirms additional session-related identifiers:

- `StoredRefreshToken`
- `fetchUserInfo`
- `getUserIdFromToken`
- `shouldRefreshToken`
- `refresh response missing access_token`
- `No refresh token stored. Session may not support refresh.`
- `[AuthContext] No user sub found, cannot refresh`
- `[AuthContext] checkAuthStatus error:`
- `[AuthContext] refreshUser error:`
- `[AuthContext] updateUser error:`

The bundle also contains a dedicated password-change route:

- `/officer-profile/change-password.tsx`

and a PIN authentication screen/function identifier:

- `authenticateWithPin`
- `AuthScreen`

The PIN path is kept outside the server-session analysis.

## Stronger session model

The presence of `shouldRefreshToken` is particularly important.

It indicates that the application has an explicit decision point for determining whether an existing access token should be refreshed.

The working model therefore becomes:

    app launch / auth context initialization
                |
                v
        retrieve stored auth state
                |
                v
          fetch user/session
                |
                v
        shouldRefreshToken?
             /       \
           no         yes
           |           |
           v           v
       continue    refresh token
                       |
                       v
                 new access token
                       |
                       v
                    continue

The exact refresh threshold is still unverified.

## User identity restoration

`getUserIdFromToken` indicates that user identity can be derived from the token/session rather than requiring the user to re-enter the username on every launch.

`fetchUserInfo` indicates a separate user-information retrieval step may occur after restoring authentication.

Do not assume these are separate network calls until runtime evidence confirms it.

## Token storage

The APK is configured with Expo SecureStore and contains token-storage related strings.

This supports a persistent secure-storage model for authentication credentials/tokens.

The exact key names are not treated as verified because the bundle is minified/packed and string proximity does not establish the write/read call graph.

## Refresh endpoint

Verified static endpoint:

    https://gw1.hss.moph.go.th/api/exchange/auth/token/refreshAccessToken

The exact request and response schema remain unverified.

## Important distinction

There are now three independently observed concepts:

1. Local app unlock
   - `authenticateWithPin`
   - biometric/Face ID UI
   - handled separately

2. Persistent server authentication
   - stored refresh token
   - access token
   - `shouldRefreshToken`
   - refresh endpoint

3. User profile/session restoration
   - `getUserIdFromToken`
   - `fetchUserInfo`
   - AuthContext functions

The implementation must not merge these concepts.

## Current answer to "why does the session stay alive?"

The strongest evidence-based explanation is:

**The app persists renewable authentication state locally, checks that state when the app starts, determines whether the access token needs refreshing, and refreshes it when necessary.**

The server-side session therefore appears continuous to the user even though individual access tokens may expire.

This does not prove that the refresh token itself is permanent. Its lifetime, rotation, revocation, and inactivity rules remain unknown.

## Next runtime proof

The next observation should focus on one question:

**When an already-authenticated app is opened, which network request occurs first?**

Record only:

- host
- path
- method
- status
- non-secret field names
- ordering/timing

Do not record token values.

Priority sequence:

    App launch
      ↓
    Secure token read
      ↓
    checkAuthStatus
      ↓
    shouldRefreshToken
      ↓
    [refresh OR user-info/resource request]
      ↓
    authenticated app

Once this sequence is observed, Smart_Osm22 can implement the same session lifecycle without guessing.

PIN and identity mechanics remain out of scope for this investigation.
