# ThaiD Authentication Boundary — Smart OSM22

## Decision

Smart_Osm22 will not invent or emulate the provider's authentication system.

The target boundary is:

`ThaiD / provider identity` → `HSS authorization/exchange` → `Smart OSM API access token` → `SmartOsm22 API adapter` → `Room local cache`

Google Account/Firebase Authentication is not the primary identity path for government Smart OSM data.

## Evidence from the supplied Smart OSM reference APK

The supplied APK bundle contains route strings for:

- `/thaid/mobile/authorize`
- `/thaid/mobile/token`
- `https://gw1.hss.moph.go.th/api/exchange/osm/auth/login`
- `https://gw1.hss.moph.go.th/api/exchange/auth/token/refreshAccessToken`
- `https://api-smartosm.hss.moph.go.th/api/v1`

It also contains token field strings:

- `access_token`
- `refresh_token`
- `expires_in`

These observations establish that ThaiD-related routes and a provider token exchange exist in the reference client. They do **not** yet establish the exact host, redirect/callback URI, HTTP method, payload, PKCE parameters, client identifier, token audience, or authorization scopes.

## What must be verified before implementation

1. Exact ThaiD authorization URL and host.
2. Redirect/callback URI used by the Android application.
3. Whether PKCE is used and the exact code challenge method.
4. Exact request to `/thaid/mobile/token`.
5. Exact exchange from ThaiD result to the Smart OSM provider session/token.
6. Exact access-token and refresh-token response schemas.
7. Required headers and token audience/scope.
8. Role separation between อสม. and เจ้าหน้าที่.
9. Logout, expiry and refresh behavior.
10. Error responses for an identity that is authenticated but not entitled to Smart OSM data.

## Security rules

- No production credentials in source control.
- No copied access/refresh tokens in fixtures or logs.
- No national IDs or health data in synthetic test fixtures.
- Do not disable TLS certificate/CA validation.
- Do not bypass hostname verification.
- Do not hardcode a provider session token.
- Do not treat Firebase anonymous authentication as government authorization.
- Do not use Google Account identity as a substitute for provider identity.

## Application architecture

### Identity boundary

A dedicated authentication abstraction should expose only application-level state, for example:

- signed-out
- authenticating
- authenticated
- authorization-denied
- token-expired
- network-error

The implementation behind that abstraction must remain replaceable until the exact ThaiD/provider flow is verified.

### Provider API boundary

The API adapter should receive a valid provider access token from the authentication/session layer. It must not create or infer identity from:

- Firebase UID
- Google email
- Android device ID
- local Room operator ID
- national ID typed into the app

### Local data boundary

Room remains the offline cache/work database. Provider identity and provider-owned records are authoritative for the government workflow.

## Verification workflow

The first end-to-end slice should be:

**ThaiD sign-in → provider authorization → access token → authenticated profile/identity → one read-only real API call → map to domain model → persist a minimal local cache record.**

Do not implement all Smart OSM routes first.

The first real workflow should use the provider's own identity and authorization, then verify one read-only OSM dataset before adding write operations.

## Current implementation status

The existing SmartOsmApiClient already supports:

- separate auth/data base URLs
- bearer access-token injection
- token mutex
- network diagnostics
- normal TLS validation

It does not yet implement the verified ThaiD flow. That is intentional.

## Evidence level

**Confirmed from supplied APK:** route strings, host strings, token field names, route families.

**Not yet confirmed:** exact ThaiD exchange sequence and request/response schema.

**Required next evidence:** controlled runtime capture of the user's own test sign-in flow, with secrets redacted, or official provider integration documentation that specifies the same flow.
