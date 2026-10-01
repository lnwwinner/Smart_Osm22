# Work History — Persistence Inference

## User-observed behavior

Observed behavior reported for the reference Smart OSM+ application:

- Uninstalling the application and reinstalling it does not remove the user's previous Work History.
- Signing in on another device shows the same previous history.

## Evidence interpretation

This behavior strongly indicates that the Work History is not solely an app-local Room/SQLite/AsyncStorage dataset.

Most plausible architecture:

    Device A
       │
       ├── sign in / authorized session
       │
       ▼
    Government / Smart OSM backend
       │
       │  persistent activity/work records
       │
       ├───────────────┐
       │               │
       ▼               ▼
    Device A         Device B
    Work History     Work History

Uninstalling Device A removes local application storage, but does not remove the server-side record. Signing into Device B retrieves the same account-associated records.

## Confidence

- Server-side persistence of at least the Work History data: HIGH confidence from observed cross-device/reinstall behavior.
- Exact endpoint: NOT YET VERIFIED.
- Exact database/service: NOT YET VERIFIED.
- Whether a local cache also exists: LIKELY; APK contains activity cache/error identifiers, but cache is not authoritative.
- Whether User Activity Logs are the same dataset: NOT VERIFIED.

## Architectural consequence for Smart_Osm22

Work History must be modeled as a synchronized remote dataset, not as a local-only history table.

Target pattern:

    ThaiD / provider authorization
              ↓
       Smart OSM API access
              ↓
       Remote Work History
              ↓
       API adapter / mapper
              ↓
       optional Room cache
              ↓
             UI

Room may cache the data for offline display, but the remote service remains the source of truth unless an official contract proves otherwise.

## Required validation

Runtime tracing should now prioritize the Work History READ operation because the cross-device behavior gives us a strong expected result.

Test sequence:

1. Device/session A: sign in.
2. Open Work History.
3. Record only request host/path/method/status and field names.
4. Device/session B: sign in with the same authorized identity.
5. Open Work History.
6. Compare request path and response structure.
7. Reinstall A and repeat.

Do not capture or store access tokens, refresh tokens, cookies, authorization codes, national IDs, or health/personal data.

## Evidence boundary

Cross-device persistence proves persistence at a shared backend or other shared remote service, but it does not by itself prove the specific HSS endpoint or whether multiple backend services participate.

Therefore this evidence upgrades the architecture decision from a hypothesis to a high-confidence persistence requirement, while leaving endpoint and schema verification pending.