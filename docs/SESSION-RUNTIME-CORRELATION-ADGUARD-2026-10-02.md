# Session Runtime Correlation — AdGuard 2026-10-02

## Source

AdGuard recent-activity export supplied for the Smart OSM reference-app session.

Only metadata is retained here. Authorization codes and other sensitive callback values are not reproduced.

## Observed sequence

The export contains this sequence around the ThaiD callback:

| Time (local export timestamp) | Package | Host | Observation |
|---|---|---|---|
| 00:01:54 | Chrome | api-thaiphc.hss.moph.go.th | TLS connections associated with the ThaiD/ThaiPHC flow |
| 00:01:55 | Chrome | api-thaiphc.hss.moph.go.th/callback | OAuth/OIDC callback occurred |
| 00:02:00 | Chrome | api-thaiphc.hss.moph.go.th/favicon.ico | Follow-up browser request |
| 00:02:52 | Smart OSM | api-thaiphc.hss.moph.go.th | Native app TLS tunnel, 739 bytes sent / 5,550 bytes received |

The native Smart OSM request is approximately 57 seconds after the callback.

## What this proves

1. The ThaiD/ThaiPHC browser flow reaches the HSS callback.
2. The Smart OSM application subsequently establishes its own TLS connection to the ThaiPHC HSS host.
3. The native request transferred substantially more response data than the callback itself.
4. The native request is therefore a strong candidate for a post-callback application-side exchange/bootstrap operation.

## What this does NOT prove

AdGuard's native TunnelRequest record exposes the host but not the HTTPS path or HTTP payload.

Therefore this evidence does not prove that the native request is specifically:
- /api/exchange/auth/token/refreshAccessToken;
- getHSSExchangeToken;
- a profile request;
- a session bootstrap request;
- or another HSS endpoint.

The exact path must be obtained from an authorized runtime trace or other non-sensitive evidence.

## Important timing observation

The observed callback-to-native-app interval is about 57 seconds.

This is consistent with a multi-stage flow in which:
- the browser completes ThaiD authorization;
- control returns to the application;
- the application performs an HSS-side exchange or bootstrap operation.

It is not evidence of a 57-second timeout or token lifetime.

## Current combined model

    ThaiD authorization
          ↓
    HSS callback
          ↓
    Smart OSM native app
          ↓
    HSS native request
          ↓
    application session/bootstrap
          ↓
    authenticated API

Separately, static APK evidence shows:
- StoredRefreshToken
- shouldRefreshToken
- refreshAccessToken endpoint
- fetchUserInfo
- getUserIdFromToken

Therefore the post-callback HSS request and the later refresh lifecycle should be treated as separate investigation targets until their paths are proven.

## Next trace target

The next controlled test should start with an already-authenticated Smart OSM installation and capture:

    open app
       ↓
    first native HSS/API request
       ↓
    subsequent native HSS/API request(s)

Record only:
- timestamp;
- host;
- path if available;
- HTTP method if available;
- response status;
- request/response field names without values.

Do not capture:
- authorization codes;
- access tokens;
- refresh tokens;
- cookies;
- passwords;
- PINs;
- citizen IDs;
- personal or health information.

TLS certificate validation must remain enabled.

This document is analysis-only and does not modify production authentication behavior.
