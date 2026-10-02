# Smart OSM+ — System Fishbone

Analysis artifact only. No production implementation is defined here.

## Core question

What is where, what does it do, where does it go, what comes back, and how does the result reach the user?

## Master fishbone

    Identity / ThaiD
        ├─ Authentication
        ├─ Authorization / entitlement
        ├─ User / OSM identity
        ├─ Profile
        ├─ Feature / Screen
        ├─ Function / Service
        ├─ API client
        ├─ Host / Endpoint
        ├─ Request
        ├─ Server / source data
        ├─ Response
        ├─ Mapper / model
        ├─ State / cache
        ├─ Activity / audit
        ├─ Error / retry
        └─ UI result
SYSTEM FLOW ---------------------------------------------------------------> RESULT

## Evidence rule

- [V] Verified — directly observed.
- [P] Partial — observed but incomplete.
- [U] Unknown — not established.
- [H] Hypothesis — working hypothesis only.

Never promote [H] to [V] without evidence.

## Feature fishbone template

    USER ACTION
       │
       ├── Screen → Component
       ├── Function → Service / Repository
       ├── Authentication → Token / session
       ├── Authorization → Role / entitlement / scope
       ├── Request
       │     ├── Method
       │     ├── Host
       │     ├── Endpoint
       │     ├── Headers
       │     ├── Query
       │     └── Body
       ├── Server
       │     ├── Gateway
       │     ├── Business service
       │     └── Source data
       ├── Response
       │     ├── Status
       │     ├── Envelope
       │     └── Data
       ├── Mapping → Domain / UI model
       ├── Local → Memory / Room / cache / Offline behavior
       ├── Activity / Audit
       │     ├── Who
       │     ├── What
       │     ├── When
       │     ├── Resource
       │     ├── Result
       │     └── Correlation
       └── RETURN → UI state → user

## First feature: Work History

Static evidence currently known from the supplied Smart OSM+ APK:

- [V] route: /(tabs)/profile/work-history/index.tsx
- [V] layout: /(tabs)/profile/work-history/_layout.tsx
- [V] styles: /(tabs)/profile/work-history/work-historyStyles.ts
- [V] function identifier: fetchWorkHistory
- [V] function identifier: getAllActivities
- [V] function identifier: _logActivity
- [V] activity fields observed: activity_type, activity_date, total_activities, workDescription
- [P] these identifiers may represent read/write activity flow, but their call-chain relationship is not yet proven.
- [U] exact production endpoint used by fetchWorkHistory.
- [U] exact payload/parameters.
- [U] whether _logActivity writes to the same data source later read by Work History.
- [U] whether Work History is server-only, cached, or hybrid.

## Training History

Current evidence is limited to training-related identifiers:

- trainingHeader
- trainingDetails

Status: [P] training feature exists in the reference bundle; exact data source and request flow remain to be traced.

## Security fishbone

    ThaiD identity
        ↓
    Token/session
        ↓
    Provider authorization
        ↓
    Smart OSM entitlement
        ↓
    API request
        ↓
    Server-side permission check
        ↓
    Authorized resource
        ↓
    Response
        ↓
    Local presentation/cache

Do not assume that possession of an app-local role, village number, or locally stored identifier is sufficient authorization.

## Data ownership principle

For government/agency data, treat the authoritative API/service as the source of truth unless runtime or documentation proves otherwise. Local Room data in Smart OSM22 should be treated as cache/workspace where appropriate, not as a replacement government registry.
