# Smart OSM+ — API and Data Flow Map

## Rule

Do not invent an endpoint. A route enters this document as verified only after the APK call chain or runtime traffic establishes it.

## Known production hosts from supplied APK

### Data API

https://api-smartosm.hss.moph.go.th/api/v1/

Known route families observed in the supplied APK include:

- /households
- /mosquito-larvae
- /reports
- /ncd-screenings
- /lookups/*
- notification-related routes
- summary/report routes

### HSS gateway

https://gw1.hss.moph.go.th/

Known observed paths include:

- api/exchange/osm/auth/login
- api/exchange/auth/token/refreshAccessToken
- api/exchange/elder/personnel

Status: these are observed strings/routes from the supplied APK; individual endpoint semantics still require call-chain verification.

## ThaiD boundary

Observed in the supplied APK:

- /(thaid)/callback.tsx
- /consent.tsx
- THAID_PUBLIC_REDIRECT_URI
- THAID_REDIRECT_URI
- THAID_CLIENT_ID
- THAID_PUBLIC_CLIENT_ID
- /thaid/mobile/authorize
- /thaid/mobile/token

The exact production values, PKCE details, scopes, and exchange payloads remain unverified.

## Target flow

    User
     │
     ▼
    Smart OSM+ UI
     │
     ├── ThaiD authorization
     │       │
     │       ▼
     │   ThaiD / DOPA
     │       │
     │       ▼
     │   callback / token
     │
     ▼
    Provider / HSS authorization
     │
     ▼
    Smart OSM API
     │
     ▼
    Authorized data
     │
     ▼
    Adapter / mapper
     │
     ├── UI state
     └── local cache where justified

## Request trace schema

For each API call record:

    TRACE
    ├── feature
    ├── screen
    ├── function
    ├── client/service
    ├── method
    ├── host
    ├── path
    ├── query parameter NAMES
    ├── body field NAMES
    ├── auth dependency
    ├── permission dependency
    ├── response status
    ├── response field NAMES
    ├── mapper
    ├── local persistence
    └── UI destination

No secrets or personal data belong in this map.

## Work History trace — pending

    Work History screen
       │
       ▼
    fetchWorkHistory()          [V: identifier exists]
       │
       ▼
    API client                  [U]
       │
       ▼
    endpoint                    [U]
       │
       ▼
    server                      [U]
       │
       ▼
    response                    [U]
       │
       ▼
    mapping                     [U]
       │
       ▼
    UI                          [P]

## Activity write/read hypothesis — pending verification

    User activity
       │
       ▼
    _logActivity()              [V: identifier exists]
       │
       ▼
    write API                   [U]
       │
       ▼
    activity store              [U]
       │
       ▼
    getAllActivities()          [V: identifier exists]
       │
       ▼
    fetchWorkHistory()          [P: possible relationship]
       │
       ▼
    Work History UI

This is deliberately marked as a hypothesis until the call graph proves it.

## Error path

    Request
      ├── 2xx → parse → map → display
      ├── 401/403 → auth/permission path
      ├── 4xx → validation/business error
      ├── 5xx → server failure
      ├── timeout → retry/offline policy
      └── network/TLS failure → diagnostic path

Never treat a network failure as permission success or silently overwrite authoritative local data.
