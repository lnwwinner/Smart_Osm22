# Smart OSM APK API Map

Source: provider-supplied Smart OSM+ APK v1.0.8 reference package.

This is a route-family map, not a claim that every route is currently enabled for every account. Methods and payload schemas are recorded only where verified; otherwise they are marked as unknown.

## A. API hosts observed in the APK

| Host | Observed purpose | Confidence |
|---|---|---|
| https://api-smartosm.hss.moph.go.th/api/v1 | Smart OSM application API | High |
| https://api-thaiphc.hss.moph.go.th/api/v1 | ThaiPHC API/mobile backend family | High |
| https://gw1.hss.moph.go.th/api/exchange | Authentication/token exchange gateway | High |
| http://192.168.1.120:8000/api/v1 | Local development/test endpoint embedded in bundle | High; not production |

The local development URL must not be used as a production endpoint.

## B. Authentication / identity route families

Observed strings include:

- `/api/exchange/osm/auth/login`
- `/api/exchange/auth/token/refreshAccessToken`
- `/thaid/mobile/authorize`
- `/thaid/mobile/token`
- `/analytics/volunteers/auth/third-party/exchange`

Related application state strings include:

- `access_token`
- `refresh_token`
- `username`
- `password`
- `incorrect_password`
- `No access_token in refresh response`
- `Login response did NOT include refresh_token`

Interpretation: the APK contains a real authentication/session lifecycle and a ThaiD-related mobile flow. The exact provider-controlled exchange sequence must not be guessed or recreated inside Smart_Osm22.

## C. Core data route families

Observed:

- `/households`
- `/mosquito-larvae`
- `/reports`
- `/ncd-screenings`
- `/lookups/provinces`
- `/lookups/districts`
- `/lookups/subdistricts`
- `/lookups/health-areas`
- `/lookups/prefix`
- `/lookups/age`

These correspond to visible/embedded Smart OSM capabilities around households, mosquito-larvae reporting, reports, NCD screening, and lookup/reference data.

## D. Summary / analytics route families

Observed route families and UI references include:

- `/mosquito-larvae/summary/monthly`
- `/mosquito-larvae/summary/weekly`
- `/ncd-screenings-yearly-summary`
- `/osm-outstandings`
- `/osm/summary`
- `/analytics/reports/map-data`
- `/analytics/notifications/mark-read`
- `/notifications/unread-count`

These should be treated as analytics/reporting/notification surfaces until request methods and payloads are verified.

## E. OSM work/report route families

The bundle contains route/UI references for:

- `/report-osm1`
- `/report-osm1/activity-data/bulk`
- `/report-osm1/submissions`
- `/report-osm1/submitted-years`
- `/report-osm1/summary`
- `/report-osm1/summary/category`
- `/report-osm1/summary/fiscal-year`
- `/report-osm1-bangkok/activity-data/bulk`
- `/report-osm1-bangkok/summary/fiscal-year`

These names show that reporting is a first-class subsystem and includes both submission and summary/analytics paths.

## F. Health-work route families

The bundle contains route/UI references for:

- `/ncd-activities/community-activities`
- `/ncd-activities/family-volunteer-visits`
- `/ncd-activities/health-advices`
- `/ncd-activities/home-visit-trackings`
- `/pregnant-women-evaluations`
- `/postpartum_women_*`
- `/vaccination_records`
- `/chronic_diseases`
- `/promote_health_*`

These are route/UI evidence only. Do not infer write permissions or exact API payloads from route names alone.

## G. Notification subsystem

Observed:

- `/notifications/unread-count`
- `/analytics/notifications/mark-read`
- UI references to `user-notifications`

This suggests a dedicated notification state in the application rather than notifications being only local UI messages.

## H. Role / application areas

The bundle contains separate route namespaces and screens for:

- OSM/VHV-facing features
- officer-facing features
- health-station features
- district-health features
- provincial-health features
- district-officer features

This supports the provider's statement that OSM and officer status/capabilities are separated.

It does NOT prove that the same token has the same permissions across those areas.

## I. Smart_Osm22 integration map

The safest integration boundary is:

```
Provider Identity / ThaiD / ThaiPHC
             |
             | approved authenticated session
             v
       API authorization
             |
             v
+----------------------------+
| Smart_Osm22 API Adapter    |
+----------------------------+
| households                 |
| mosquito-larvae            |
| reports                    |
| NCD screenings             |
| lookups                    |
| notifications              |
| analytics/report summaries |
+-------------+--------------+
              |
              v
       Smart_Osm22 Domain
              |
       +------+------+
       |             |
       v             v
      Room          UI
    offline cache  extensions
```

## J. What is NOT yet verified

1. Exact HTTP method for every route.
2. Exact request body/query schema for every route.
3. Exact response JSON schema.
4. Exact token exchange sequence between ThaiD, the reference app, and ThaiPHC.
5. Exact permission matrix for OSM vs officer.
6. Which endpoints are intended for third-party applications.
7. Production API rate limits and pagination rules.
8. Exact token audience/scope/claims.

These should be verified from an approved API contract or legitimate runtime test session, not inferred from route names.

## K. Development rule

Do not expand Smart_Osm22 by copying every route.

Instead:

1. Select one real OSM workflow.
2. Identify its route family.
3. Verify authorization boundary.
4. Verify request/response contract.
5. Build an adapter.
6. Add domain mapping.
7. Add Room cache/offline behavior.
8. Add UI/extension.
9. Test with synthetic data and approved runtime data separately.

This keeps the application maintainable and avoids coupling it to undocumented internal routes.
