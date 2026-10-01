# Smart OSM Reference Architecture

Status: analysis/reference only

This document records architecture signals supported by the provider-supplied Smart OSM APK, observed behavior, provider guidance, and public HSS/ThaiPHC information. Verified observations are separated from assumptions that still require an approved API contract or runtime verification.

## 1. Working boundary

Smart_Osm22 is treated as an extension/API-consumer application.

It does not recreate the reference application's identity system, ThaiD authentication flow, or internal token-exchange implementation. Identity and authorization remain an external boundary until an approved integration contract is available.

## 2. Evidence levels

### Verified from the reference APK / observed behavior

- Smart OSM API hosts and multiple API route families are present in the APK.
- ThaiD-related routes/components, callback handling, token-related logic, and secure token storage components are present.
- Access-token and refresh-token concepts are present.
- The application contains separate OSM/officer-related paths.
- Deep-link/callback configuration is present.
- Screen-capture protection is observable in testing for at least some screens, while other screens/features allow image capture.
- Presence of code does not by itself prove that every route or future feature is currently enabled for production accounts.

### Provider guidance

- The reference application is intended to participate in identity verification for the ThaiPHC ecosystem.
- OSM and officer login/status are treated separately.
- An API becomes usable after the user's sign-in has the correct status/authorization.
- Extensions may use the API without reproducing the identity system.
- Future ThaiPHC/OSM Cyber learning flows may use the application's identity/verification capability.

### Public system context

- ThaiPHC is the HSS system for managing village health volunteers.
- HSS public material describes Smart OSM as a digital work/reporting tool for OSM.
- HSS specification material requires Smart OSM/related applications to work with ThaiPHC and supports integration with other applications.

## 3. Target architecture

```
External Identity / Authorization
          |
          | approved session / API authorization
          v
+-----------------------------+
| Smart OSM API               |
+--------------+--------------+
               |
               v
+-----------------------------+
| Smart_Osm22 API Adapter     |
| - authorization boundary    |
| - request/response mapping  |
| - diagnostics               |
+--------------+--------------+
               |
               v
+-----------------------------+
| Domain / Use Cases          |
+--------------+--------------+
               |
        +------+------+
        |             |
        v             v
   Room cache     UI / reports
   offline-first  / extensions
```

## 4. Explicit non-goals

- Do not embed production credentials.
- Do not copy or recreate the provider's identity implementation unless an explicit integration contract requires it.
- Do not invent undocumented request/response schemas.
- Do not disable TLS/CA validation.
- Do not commit real OSM personal/health data as fixtures.
- Do not replace main with experimental API assumptions.

## 5. Smart_Osm22 implementation direction

### API layer

Keep endpoints and DTOs isolated behind an adapter/repository boundary. Exact payloads remain conservative until verified from an approved test environment or contract.

### Local data

Room remains useful as an offline cache and working data store. It must not be treated as the authoritative government identity source.

### Authentication boundary

The application should support an external authorized session/token boundary without requiring the identity provider's internal implementation to be copied into the app.

### Testing

Use synthetic fixtures for unit/integration tests. Runtime API tests should be enabled only when a legitimate test credential/session and approved API environment are available.

## 6. Next investigation targets

1. Map reference APK screens to API route families.
2. Identify OSM vs officer capability boundaries.
3. Map token/session lifecycle without extracting or exposing credentials.
4. Identify which API operations are read-only vs write-capable.
5. Map data models to Smart_Osm22 domain models.
6. Validate real request/response contracts only when the API is legitimately available.
7. Design extension features around real OSM workflows and user feedback.

## 7. Current project safety boundary

- `main`: production/base branch; do not alter from this analysis.
- `build/test-apk-2026-10-01`: test-build integration branch.
- `fix/backup-restore-sync-integrity-2026-10-02`: backup/restore regression branch.
- This branch: reference architecture analysis only.
