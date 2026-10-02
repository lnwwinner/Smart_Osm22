# Smart OSM22 — System Reverse Engineering

## Purpose

This branch is an isolated analysis workspace for reverse-engineering the supplied Smart OSM+ reference application before implementing further production behavior in Smart OSM22.

The goal is to map **what is where, what calls what, where data goes, what comes back, and where activity/audit information is produced or consumed**.

## Method

For each feature, trace:

1. UI entry point
2. Screen/component
3. Function/use-case/service
4. API client
5. HTTP method
6. Host and endpoint
7. Request headers/parameters/body (names only; never secrets)
8. Authentication/authorization dependency
9. Server response
10. Response mapping/model
11. Local cache/state
12. UI rendering
13. Activity/audit/log behavior
14. Error and retry path

## Evidence status

- 🟢 Verified: directly observed in supplied APK, runtime capture, API evidence, or authoritative documentation.
- 🟡 Partial: evidence exists but the complete call chain is not yet established.
- 🔴 Unknown: not established.
- 🔵 Hypothesis: useful working hypothesis that must be verified.

## First target: Work History

Known static APK evidence currently includes:

- Work History route: `/(tabs)/profile/work-history/index.tsx`
- Work History layout: `/(tabs)/profile/work-history/_layout.tsx`
- Work History styles: `/(tabs)/profile/work-history/work-historyStyles.ts`
- Function identifier: `fetchWorkHistory`
- Function identifier: `getAllActivities`
- Function identifier: `_logActivity`
- Activity-related fields observed: `activity_type`, `activity_date`, `total_activities`, `workDescription`

These are **static observations only**. They do not yet prove that these functions call the same endpoint or form one continuous data flow.

## Initial fishbone

```
Identity / ThaiD
       ├── Authentication
       ├── Authorization / entitlement
       ├── Profile identity
       ├── Work History UI
       ├── Function / service
       ├── API / endpoint
       ├── Request
       ├── Server / data source
       ├── Response
       ├── Mapping / state
       ├── Local cache
       └── Activity / Audit
                    ↓
                 UI result
```

## Rules

- Do not guess production endpoints, payloads, scopes, callback URIs, or token schemas.
- Do not commit production credentials, tokens, national IDs, health data, or personal-data fixtures.
- Do not bypass TLS/CA validation.
- Do not treat Room as the source of truth for government data.
- Do not merge this analysis branch into main until evidence is reviewed.
- Runtime captures must redact authorization codes, access/refresh tokens, cookies, national IDs, and health data.
- Keep verified facts separate from hypotheses.

## Deliverables

The analysis should eventually produce:

- System-level fishbone
- Feature-level fishbones
- API/data-flow map
- Authentication/authorization flow
- Work History trace
- Training History trace
- Activity creation/read-back trace
- Logging/audit model
- Evidence register linking each conclusion to its source
