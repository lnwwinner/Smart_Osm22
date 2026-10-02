# Activity Subsystem — Static Trace v3

## New evidence from byte-level bundle inspection

Further byte-context inspection of the supplied Smart OSM+ v1.0.8 Hermes bundle produced several useful associations.

### 1. UserActivityData feature identifiers

The bundle contains:

- getUserActivityData
- UserActivityData
- UserActivityDataBarItemTitleFontColorActive
- getUserByExternalId
- getUserFromStorage

These occur in the application's identifier/string table. This strengthens the conclusion that UserActivityData is a named application feature and that user identity retrieval/storage is present nearby in the same bundle.

Important: string-table proximity is NOT proof that getUserByExternalId is called by getUserActivityData. It is only static contextual evidence.

### 2. Activity write identifiers

The bundle contains:

- saveActivityData
- saveBatchActivityData
- activity_identity
- activity2_count
- activityItem

The identifiers are distinct from getUserActivityData/fetchWorkHistory, so the reference application has explicit naming for both user activity retrieval and activity writing.

### 3. _logActivity context

Byte context around _logActivity contains other application identifiers including:

- localId
- saveNotesToBackend
- logoutButtonText

This is suggestive of a broader application-side activity/event mechanism, but the string table cannot prove the runtime call relationship. Therefore _logActivity remains unclassified as local event logging vs server audit logging.

### 4. Route table correction

/user-activity-logs/logs appears embedded in a concatenated React Navigation route table together with dashboard, menu/filterstatistics, report, ThaiD, and other screen paths.

Therefore:

- [V] /user-activity-logs/logs is a screen/navigation route.
- [U] It is a backend API endpoint.
- [U] It is the destination of _logActivity.

Do not send HTTP requests to this path merely because the string exists.

## Revised activity fishbone

    USER
      │
      ├───────────────┐
      │               │
      ▼               ▼
UserActivityData   Activity Write
      │               │
      ├─ getUser...   ├─ saveActivityData
      │               ├─ saveBatchActivityData
      │               └─ activity_identity
      │
      ▼
   read path ?
      │
      ▼
getAllActivities ?
      │
      ▼
fetchWorkHistory ?
      │
      ▼
Work History UI

Separate branch:

    Application events
          │
          ▼
    _logActivity ?
          │
          ├── local event/cache ?
          ├── backend audit API ?
          └── User Activity Logs screen ?

All ? markers remain unresolved.

## Strongest conclusion at this stage

The APK contains at least two independently named activity concepts:

1. User/work activity data retrieval and presentation.
2. Activity creation/edit/bulk-save operations.

A third concept, User Activity Logs, exists as a navigation destination.

That is enough to prevent us from designing Smart_Osm22's Work History as a generic audit-log table.

## Next extraction step

Static extraction has now reached the point where additional string scanning is unlikely to establish the call graph reliably. The next decisive evidence is controlled runtime metadata capture of the reference APK.

Required trace actions:

1. Open UserActivityData/Work History.
2. Observe network calls.
3. Create one synthetic/test activity if the app permits.
4. Save it.
5. Edit it.
6. Perform bulk save if available.
7. Reopen Work History.
8. Open User Activity Logs.
9. Compare request sequences.

Record only host, method, path, status, non-secret field names, and ordering. Redact tokens, cookies, authorization codes, national IDs, and health/personal data.

TLS validation must remain enabled.

## Status

- UserActivityData feature identifiers: VERIFIED
- Activity write identifiers: VERIFIED
- Work History identifiers: VERIFIED
- User Activity Logs screen route: VERIFIED
- Work History endpoint: UNKNOWN
- Activity write endpoint/method: UNKNOWN
- Audit endpoint: UNKNOWN
- _logActivity classification: UNKNOWN
- Runtime trace: REQUIRED