# Work History — Deep Static Findings (2026-10-02)

## New evidence extracted directly from supplied Smart OSM+ v1.0.8 APK

The React Native bundle contains the following distinct activity-related identifiers/messages:

- `fetchWorkHistory`
- `getAllActivities`
- `_logActivity`
- `Get user activity data error:`
- `Get activity data error:`
- `handleConfirmSave activity data error:`
- `Save bulk activity data error:`
- `handleEditSave batch activity data error:`
- `CacheGet activities error:`
- `activity_type`
- `activity_date`
- `total_activities`
- `workDescription`
- `citizenId`
- `citizenIdNumber`
- `activity_category`

These observations strengthen the conclusion that the reference app has more than a visual Work History screen: it contains separate concepts for **reading activity data, saving activity data, bulk saving, and activity logging**.

## Important route evidence

The bundle also contains route strings:

- `/report-osm1/activity-data/bulk`
- `/report-osm1-bangkok/activity-data/bulk`
- `/report-osm1/submissions`
- `/report-osm1/submitted-years`
- `/report-osm1/summary`
- `/report-osm1/summary/category`
- `/report-osm1/summary/fiscal-year`

These are confirmed as strings/routes embedded in the bundle. They are **not yet proven to be the endpoint used by Work History**.

## Current fishbone update

    Work History UI
       │
       ├── fetchWorkHistory()                         [V identifier]
       │       │
       │       └── activity retrieval path             [P]
       │
       ├── getAllActivities()                         [V identifier]
       │       │
       │       └── activity collection path            [P]
       │
       ├── _logActivity()                             [V identifier]
       │       │
       │       └── activity logging path               [P]
       │
       ├── Save activity / bulk activity messages      [V]
       │       │
       │       └── write path                          [P]
       │
       ├── citizenId / citizenIdNumber                 [V]
       │       │
       │       └── identity/resource linkage            [U]
       │
       └── activity fields                             [V]
               ├── activity_type
               ├── activity_date
               ├── total_activities
               └── workDescription

## What this changes

The previous hypothesis was:

    _logActivity()
        ↓
    activity store
        ↓
    getAllActivities()
        ↓
    fetchWorkHistory()
        ↓
    Work History

This remains **HYPOTHESIS**, but there is now stronger evidence of a broader activity subsystem because the same bundle contains both read-related and save/bulk-save activity messages.

We still must not conclude that:

- Work History = Audit Log
- _logActivity = server audit event
- getAllActivities = Work History API
- /report-osm1/activity-data/bulk = Work History endpoint
- citizenId is the authorization identity

until the call graph or runtime traffic proves it.

## Next extraction target

Trace these in this order:

1. `fetchWorkHistory` references and surrounding module.
2. `getAllActivities` references and surrounding module.
3. `_logActivity` references and surrounding module.
4. Search for the exact error strings and identify the enclosing service/module.
5. Trace Axios/client calls from that module.
6. Resolve the actual endpoint and method.
7. Compare the Work History read path with activity write/bulk-write paths.
8. Determine whether there is a separate audit/logging service.
9. Determine whether `citizenId` is a resource key, actor identity, or both.
10. Verify with runtime metadata only if static analysis cannot establish the chain.

## Security note

The APK also contains authentication-related strings and access-token handling. Do not extract, publish, or commit actual tokens, cookies, authorization codes, or personal data while performing this analysis.
