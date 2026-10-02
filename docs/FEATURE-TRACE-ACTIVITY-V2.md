# Work History — Call-Graph Investigation v2

## New static evidence

Direct extraction from the supplied Smart OSM+ v1.0.8 bundle found additional activity-related identifiers and route strings:

### Activity data operations

- getAllActivities
- _logActivity
- saveActivityData
- saveBatchActivityData
- activity_identity
- activity2_count

### Activity UI/error paths

- Get user activity data error:
- Get activity data error:
- handleConfirmSave activity data error:
- handleEditSave batch activity data error:
- Save bulk activity data error:
- CacheGet activities error:

### Navigation / feature identifiers

- UserActivityData
- getUserActivityData
- UserActivityDataBarItemTitleFontColorActive

### Potential server routes observed in the bundle

- /report-osm1/activity-data/bulk
- /report-osm1-bangkok/activity-data/bulk
- /report-osm1/submissions
- /report-osm1/submitted-years
- /report-osm1/summary
- /report-osm1/summary/category
- /report-osm1/summary/fiscal-year

### Separate user-activity-log route

The bundle also contains the route string:

- /user-activity-logs/logs

This is important evidence of a distinct **User Activity Logs** feature/route.

**Important:** this string is currently proven only as a navigation/route string embedded in the bundle. It is not yet proven to be the API endpoint that receives audit events.

## Revised system model

There are now at least three concepts that must remain separate until proven equivalent:

### A. Work History

Purpose appears to be presenting the user's work/activity history.

Known identifiers:
- fetchWorkHistory
- getAllActivities
- activity_type
- activity_date
- workDescription

### B. Activity Data

Purpose appears to include creation/editing/bulk-saving of work activity records.

Known identifiers:
- saveActivityData
- saveBatchActivityData
- handleConfirmSave
- handleEditSave
- activity_identity

### C. User Activity Logs

A distinct route exists:

- /user-activity-logs/logs

This may represent audit/history of application/system actions rather than work activity records.

## Critical distinction

Do NOT equate:

    Work History
    ≠ Activity Data
    ≠ User Activity Logs

unless the call graph or runtime evidence proves the relationship.

This distinction is now a primary investigation target.

## Revised fishbone

    USER
      │
      ├───────────────────────────────────────────────┐
      │                                               │
      ▼                                               ▼
  WORK ACTIVITY                                  SYSTEM ACTION
      │                                               │
      ├─ saveActivityData                              ├─ user activity log?
      ├─ saveBatchActivityData                         │
      ├─ activity_identity                             └─ /user-activity-logs/logs
      │
      ▼
  ACTIVITY DATA STORE
      │
      ├─ getAllActivities
      ├─ fetchWorkHistory
      │
      ▼
  WORK HISTORY UI

Status of connecting lines: [P]/[U] until call sites/runtime confirm them.

## Next call-graph targets

1. Locate the module surrounding getUserActivityData/UserActivityData.
2. Locate every reference to saveActivityData.
3. Locate every reference to saveBatchActivityData.
4. Locate every reference to _logActivity.
5. Locate every reference to fetchWorkHistory.
6. Compare their imports/dependencies.
7. Identify the API abstraction each function uses.
8. Identify endpoint + method.
9. Determine whether /user-activity-logs/logs is a screen route or API route only.
10. Determine whether activity records and audit records have different schemas.
11. Determine actor identity vs subject/resource identity.
12. Determine whether the app caches activity data and how invalidation occurs.

## Security interpretation

If User Activity Logs are a separate audit system, Smart OSM22 should not attempt to recreate authoritative audit records locally. The client can emit required events, but the authoritative audit trail should remain at the service boundary if that is how the real system operates.

No credentials, tokens, cookies, national IDs, or health data are included in this analysis artifact.
