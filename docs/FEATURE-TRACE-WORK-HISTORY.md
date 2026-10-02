# Feature Trace — Work History

## Objective

Determine the complete path:

tap → screen → function → service → request → server → response → mapping → state/cache → UI → activity/audit

## 1. Entry

- Route: /(tabs)/profile/work-history/index.tsx
- Layout: /(tabs)/profile/work-history/_layout.tsx
- Status: V

## 2. Data loader

- fetchWorkHistory
- Status: V as a static identifier.
- Exact implementation/call graph: U

## 3. Activity identifiers

- getAllActivities — V identifier.
- _logActivity — V identifier.
- activity_type — V field identifier.
- activity_date — V field identifier.
- total_activities — V field identifier.
- workDescription — V field identifier.

## 4. What we must prove

### A. Read path

    Work History screen
          ↓
    fetchWorkHistory()
          ↓
    ?
          ↓
    ?
          ↓
    API response
          ↓
    ?
          ↓
    Work History state
          ↓
    UI

### B. Write path

    User performs activity
          ↓
    ?
          ↓
    _logActivity()
          ↓
    ?
          ↓
    API / local store
          ↓
    activity record

### C. Relationship

Determine whether:

    _logActivity()
          ↓
    Activity store
          ↓
    getAllActivities()
          ↓
    fetchWorkHistory()
          ↓
    Work History

is true, false, or only partially true.

## 5. Evidence collection order

1. Find every reference to fetchWorkHistory.
2. Identify the function body.
3. Identify imports used by the function.
4. Trace the next function/service call.
5. Identify the API abstraction.
6. Resolve host and endpoint.
7. Identify request field names.
8. Identify response field names.
9. Trace mapper/state update.
10. Trace UI rendering.
11. Separately trace _logActivity.
12. Compare the two call graphs.
13. Verify at runtime if static analysis cannot establish the link.

## 6. Expected output

    [UI]
      ↓
    [Function]
      ↓
    [Service]
      ↓
    [HTTP method + endpoint]
      ↓
    [Auth]
      ↓
    [Authorization]
      ↓
    [Server]
      ↓
    [Response]
      ↓
    [Mapper]
      ↓
    [State/cache]
      ↓
    [UI]

Every box must carry an evidence status.

## 7. Security checks

Do not record:

- access token
- refresh token
- authorization code
- cookies
- national ID
- health information

Record only metadata necessary to establish the contract.

## 8. Completion criteria

Work History is considered mapped only when we can answer:

1. Where does the screen start?
2. Which function loads the data?
3. Which client sends the request?
4. Which host/endpoint receives it?
5. What non-secret inputs are sent?
6. How is identity established?
7. How is authorization enforced?
8. What response comes back?
9. How is it mapped?
10. Where is it stored?
11. How is it rendered?
12. Where does activity logging happen?
13. How do failures return to the user?

Until then, the feature remains partially mapped.
