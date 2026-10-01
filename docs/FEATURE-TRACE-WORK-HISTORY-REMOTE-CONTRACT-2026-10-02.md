# Work History — Remote Persistence Contract v1

## New behavioral evidence

User reports that after uninstalling the reference Smart OSM+ app, reinstalling it, and signing in again, previous Work History remains available. The same history also appears when the same user signs in on another device.

## Contract inferred from behavior

Expected invariant:

    Same authorized user identity
             +
       any supported device
             +
       fresh installation
             ↓
       same server-side history

This is now a required behavioral invariant for Smart_Osm22's Work History integration.

## What this proves

- Work History is not dependent solely on the original device's local application database.
- A shared remote service or synchronized backend retains enough information to reconstruct the history after reinstall/cross-device sign-in.
- A local cache may still exist, but it cannot be treated as the authoritative source.

## What this does NOT prove

- Exact backend host/path.
- Exact HTTP method.
- Exact identity key used by the backend.
- Whether history is stored in one service or composed from several services.
- Whether User Activity Logs and Work History share a datastore.

## Government-system corroboration

HSS describes Smart อสม. as a tool for OSM work and reporting, and official material describes online reporting of OSM work through the Smart อสม. application. This supports the interpretation that work/report data is part of a service-backed workflow rather than merely local UI state. citeturn0search12turn0search14

## Smart_Osm22 design rule

Do not implement Work History as:

    Room -> history = source of truth

Implement the boundary as:

    Provider authorization
          ↓
    Smart OSM remote service
          ↓
    Work History API
          ↓
    DTO / mapper
          ↓
    Room cache (optional)
          ↓
    UI

## Runtime verification plan

Use the user's legitimate reference-app session and record metadata only:

1. Open Work History on device A.
2. Record host/path/method/status and non-secret field names.
3. Sign in on device B.
4. Open Work History.
5. Compare endpoint and response shape.
6. Reinstall device A.
7. Sign in again.
8. Open Work History and compare again.

Success criterion:

    same authorized identity → same logical history

with expected differences only for newly created records or server-side updates.

Never capture or store access tokens, refresh tokens, cookies, authorization codes, national IDs, or health/personal payload values. Keep TLS/CA validation enabled.

## Implementation consequence

Once the endpoint contract is verified, Smart_Osm22 should expose Work History through an API repository/use-case and use Room only as a cache/offline presentation layer. Sync should be keyed by the provider's verified stable activity identity, not by device-local IDs.