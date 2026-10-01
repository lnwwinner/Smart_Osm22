# Smart OSM+ — Evidence Register

## Purpose

Keep reverse-engineering conclusions auditable. Each claim must identify its evidence source and confidence.

## Status legend

- V — Verified: directly observed in supplied artifact/runtime or authoritative source.
- P — Partial: evidence exists but a link in the chain is missing.
- U — Unknown: not established.
- H — Hypothesis: working interpretation requiring verification.

## Current register

| ID | Area | Finding | Status | Evidence |
|---|---|---|---|---|
| E-001 | Work History | Work History route exists | V | Supplied Smart OSM+ APK bundle |
| E-002 | Work History | fetchWorkHistory identifier exists | V | Supplied APK bundle |
| E-003 | Activity | getAllActivities identifier exists | V | Supplied APK bundle |
| E-004 | Activity | _logActivity identifier exists | V | Supplied APK bundle |
| E-005 | Activity | activity_type / activity_date / workDescription observed | V | Supplied APK bundle |
| E-006 | Training | trainingHeader / trainingDetails observed | V | Supplied APK bundle |
| E-007 | ThaiD | Dedicated callback/configuration identifiers exist | V | Supplied APK bundle |
| E-008 | ThaiD | Production client ID/redirect values | U | Not found in plaintext |
| E-009 | ThaiD | Exact production PKCE configuration | U | Not established |
| E-010 | Auth | Token exchange schema | U | Must be verified |
| E-011 | API | Smart OSM data host exists | V | Supplied APK bundle |
| E-012 | API | HSS gateway host exists | V | Supplied APK bundle |
| E-013 | API | Exact Work History endpoint | U | Call chain not yet traced |
| E-014 | API | Exact Training endpoint | U | Call chain not yet traced |
| E-015 | Audit | UI Work History equals audit log | H | Must not be assumed |
| E-016 | Data | Government API is authoritative for agency data | P | HSS documentation + architecture evidence |
| E-017 | Local | Room is local cache/workspace in target architecture | P | Smart OSM22 architecture decision |
| E-018 | Security | TLS validation must remain normal | V | Project security rule |

## Required next evidence

### Work History

1. Screen entry point → function call.
2. fetchWorkHistory implementation/call site.
3. API client invoked by that function.
4. Host + endpoint.
5. HTTP method.
6. Non-secret parameter names.
7. Response shape.
8. Mapping to UI model.
9. Cache behavior.
10. Relationship, if any, to _logActivity.

### Training

1. Screen entry point.
2. Data loading function.
3. API client.
4. Endpoint.
5. Response.
6. Read-only vs write behavior.

### Runtime

Capture only metadata:

- host
- path
- method
- parameter/header names
- status code
- response field names
- request/correlation identifier if non-sensitive

Redact authorization codes, access/refresh tokens, cookies, national IDs, health information, and personally identifying payload values.

## External corroboration

HSS Data Gateway currently exposes an API Gateway category and a village health volunteer data category.

An HSS document describes Smart OSM connections involving ThaiPHC registration data, OSM data, population data in responsible areas, and doctor/health-service relationships.

These sources support the overall API/data-integration direction but do not prove individual APK endpoint mappings.
