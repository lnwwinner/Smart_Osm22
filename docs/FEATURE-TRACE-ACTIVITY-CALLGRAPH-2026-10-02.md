# Activity Call-Graph Investigation — 2026-10-02

## Evidence level

The supplied Smart OSM+ v1.0.8 Android bundle is Hermes bytecode. Static string extraction can confirm embedded identifiers and route strings, but does not by itself recover JavaScript call sites.

### Confirmed from bundle

| Item | Evidence | Interpretation |
|---|---|---|
| getAllActivities | embedded identifier | Activity-related function/identifier exists |
| fetchWorkHistory | embedded identifier | Work-history-related function/identifier exists |
| saveActivityData | embedded identifier | Activity save operation exists |
| saveBatchActivityData | embedded identifier | Batch activity save operation exists |
| activity_identity | embedded identifier | Activity identity field/identifier exists |
| CacheGet activities error: | embedded error string | Activity cache/read path exists |
| handleConfirmSave activity data error: | embedded error string | Activity save UI/error path exists |
| handleEditSave batch activity data error: | embedded error string | Batch edit/save UI/error path exists |
| Save bulk activity data error: | embedded error string | Bulk save error path exists |
| /report-osm1/activity-data/bulk | embedded route | Server route string exists |
| /report-osm1-bangkok/activity-data/bulk | embedded route | Bangkok variant route string exists |
| /user-activity-logs/logs | embedded route | A navigation/screen route exists |

## Important new finding

The /user-activity-logs/logs string occurs inside a larger navigation route table containing screen paths such as /dashboard/main, /menu/filterstatistics/OtherActivityScreen, /report-osm1/..., /thaid/mobile/..., and /user-activity-logs/logs.

Therefore the current evidence supports:

> /user-activity-logs/logs is a navigation/screen route in the app.

It does not prove that /user-activity-logs/logs is the backend HTTP endpoint.

## Current call-graph status

The Hermes bundle does not expose readable source-level call sites through plain string extraction. These relationships remain UNVERIFIED:

fetchWorkHistory
  ?-> getAllActivities
  ?-> API client
  ?-> /report-osm1/...

saveActivityData
  ?-> API client
  ?-> /report-osm1/activity-data/bulk

saveBatchActivityData
  ?-> API client
  ?-> /report-osm1/activity-data/bulk

_logActivity
  ?-> local activity cache
  ?-> User Activity Logs
  ?-> server audit endpoint

No one of these links should be implemented as fact yet.

## What static evidence can establish now

The activity feature appears to have at least:
1. A work-history/read concept.
2. Activity create/edit/bulk-save concepts.
3. An activity cache/error path.
4. A separate User Activity Logs navigation destination.
5. Report/activity API route strings.

## Next verification target

Use runtime observation of the user's own reference APK while performing controlled actions: open Work History; open an existing activity; create/save one test activity; edit/save it; use any bulk-save operation; open User Activity Logs.

Capture only host, method, path, status, non-secret field names, request ordering, and whether the response is cached/local/network.

Redact authorization codes, access/refresh tokens, cookies, national IDs, personal information, and health information.

Do not disable TLS validation or use a trust-all interceptor.

## Engineering consequence

Smart_Osm22 should not create an Audit Log implementation based solely on _logActivity or /user-activity-logs/logs.

First prove whether the government service has an authoritative audit/event endpoint. If it does, the client should integrate with that boundary rather than inventing a local authoritative audit trail.

## Status

- Static evidence: CONFIRMED
- Navigation route: CONFIRMED
- Backend endpoint for User Activity Logs: NOT CONFIRMED
- Work History API endpoint: NOT CONFIRMED
- Activity Data API method/payload: NOT CONFIRMED
- Audit-log relationship: NOT CONFIRMED
- Runtime verification: PENDING

No credentials, tokens, PII, or health data are included.