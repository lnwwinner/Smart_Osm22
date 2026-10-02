# Smart OSM Authentication Boundary — 2026-10-02

## Scope

For Smart_Osm22 reverse engineering, **PIN mechanics and the exact relationship between PINs and identity are intentionally out of scope for now**.

The reference application demonstrates a normal authenticated-session model:
- the user does not need to enter a username every time the application is opened;
- the application can remain associated with the authenticated account/session;
- the Settings area provides a password-change function;
- re-entry/unlock behavior can therefore be treated separately from the initial account identification flow.

The immediate engineering goal is to reproduce the **server-side authentication/session and API contract**, not to redesign the application's PIN behavior.

## What we need to establish

The priority is:

    Initial authentication
          ↓
    ThaiD / Smart OSM identity flow
          ↓
    Smart OSM / HSS session
          ↓
    API authorization
          ↓
    authenticated API requests
          ↓
    session persistence / refresh
          ↓
    normal app re-entry without repeated username entry

The exact PIN implementation can be handled later once the backend/session contract is known.

## Password change

The reference application exposes password management in Settings.

Therefore Smart_Osm22 should eventually model password management as an authenticated account operation, for example:

    Settings
       ↓
    Change Password
       ↓
    authenticated Smart OSM account/session
       ↓
    backend password-change API

The exact endpoint, HTTP method, request fields, and validation rules remain unverified and must be obtained from runtime evidence or the reference application's API contract.

## Important separation

Do not spend implementation effort on:
- guessing whether the ThaiD PIN and Smart OSM password share digits;
- guessing PIN storage;
- guessing biometric fallback rules;
- forcing username entry on every launch;
- treating local app unlock as the primary API authentication mechanism.

Instead, establish:
- account identity;
- server session/token;
- token refresh/expiry;
- authorization/entitlement;
- password-change API;
- authenticated resource APIs.

## Session behavior

The working product requirement is:

**After successful authentication, the user should not have to enter the username on every application launch.**

This does not mean that a token should be stored insecurely. The implementation should use secure platform storage for credentials/tokens and restore the authenticated session according to the actual Smart OSM contract.

If the server session expires or is revoked, the application should follow the reference application's re-authentication behavior.

## Current reverse-engineering priority

1. ThaiD callback and token exchange.
2. Smart OSM/HSS account/session exchange.
3. First authenticated API request.
4. Access-token/session refresh.
5. Account/profile endpoint.
6. Password-change endpoint.
7. Work History remote endpoint.
8. Other resource APIs.

PIN details are deliberately deferred.

## Security

Do not capture or commit:
- authorization codes;
- access/refresh tokens;
- cookies;
- citizen ID numbers;
- names;
- health information;
- passwords or PINs.

Keep TLS/CA validation enabled. Never use a trust-all interceptor or certificate-validation bypass.

This is an analysis/architecture boundary only. It does not change production authentication behavior.
