# Smart OSM Authentication Layer Model — 2026-10-02

## Purpose

This document separates the authentication mechanisms observed in the reference Smart OSM+ application. The ThaiD transaction PIN must not be conflated with the Smart OSM application credential or local app-lock credential.

## Verified observations

### 1. Smart OSM exposes multiple entry paths

The reference login screen contains:
- username/password login for the application;
- a dedicated Smart OSM/OSM login path;
- a ThaiID/ThaiD entry path.

This establishes that ThaiD is an authentication/identity entry mechanism, not proof that the ThaiD PIN is the Smart OSM account password.

### 2. ThaiD consent is an identity transaction

The observed ThaiD consent screen requests:
- citizen identification number;
- Thai given name;
- Thai family name.

The transaction explicitly requires identity verification by PIN.

Therefore, the PIN shown on this ThaiD consent screen is classified as:

**THAID_TRANSACTION_PIN**

It must not be mapped to:
- SMART_OSM_ACCOUNT_PASSWORD;
- SMART_OSM_ACCOUNT_PIN;
- LOCAL_APP_LOCK_PIN;

unless runtime evidence proves such a relationship.

### 3. Static APK evidence supports a dedicated ThaiD flow

The reference APK contains identifiers/routes including:
- THAID_CLIENT_ID
- THAID_PUBLIC_CLIENT_ID
- THAID_REDIRECT_URI
- THAID_PUBLIC_REDIRECT_URI
- /thaid/mobile/authorize
- /thaid/mobile/token
- thaid_token_exchange_failed
- missing_client_id

The APK also contains OIDC-related scopes/claims and token handling identifiers.

These observations support a dedicated ThaiD/OIDC authorization and token-exchange flow.

## Authentication layers

The working model is:

    [User]
       |
       +--> [ThaiD identity verification]
       |       |
       |       +--> identity attributes / authorization result
       |
       +--> [Smart OSM account authentication]
       |       |
       |       +--> application session / API authorization
       |
       +--> [Local app unlock]
               |
               +--> PIN or biometric unlock of an existing session

These are separate security domains until runtime evidence proves otherwise.

## Important distinction

A successful ThaiD transaction proves an identity assertion/authorization result from ThaiD.

It does **not**, by itself, prove:
- the exact Smart OSM API token format;
- the Smart OSM account password;
- the local app-lock credential;
- the API entitlement/role mapping;
- the exact endpoint used after ThaiD callback.

The Smart OSM/HSS service can still perform its own account, role, status, and entitlement checks after ThaiD authentication.

## Working end-to-end model

    Smart OSM
       |
       | ThaiD login
       v
    ThaiD authorization
       |
       | verified identity/token
       v
    Smart OSM / HSS identity + entitlement layer
       |
       | authorized application session
       v
    Smart OSM API Gateway / resource APIs
       |
       v
    Work History / reports / household / health-work data

The exact token exchange and entitlement endpoint remain **UNVERIFIED** until captured from a legitimate reference-app session.

## Runtime evidence still required

Capture metadata only from the user's authorized reference-app session:

1. ThaiD authorize request
   - host
   - path
   - method
   - non-secret parameter names
   - redirect URI

2. ThaiD callback/token exchange
   - host
   - path
   - method
   - response status
   - non-secret response field names

3. Smart OSM post-ThaiD exchange
   - host
   - path
   - method
   - request field names
   - response field names
   - status

4. First authenticated resource request
   - host
   - path
   - method
   - status
   - non-secret query/body field names

5. Compare with username/password login
   - determine whether both paths converge to the same application session/token type.

Do not capture, store, commit, or transmit:
- authorization codes;
- access tokens;
- refresh tokens;
- cookies;
- citizen ID numbers;
- names;
- health information;
- other personal data.

TLS/CA verification must remain enabled. Do not use a trust-all interceptor or certificate-validation bypass.

## Classification

| Item | Status |
|---|---|
| ThaiD is a distinct login path | VERIFIED |
| ThaiD consent requests citizen ID + Thai name | VERIFIED |
| ThaiD transaction requires PIN | VERIFIED from observed UI |
| ThaiD PIN = Smart OSM account PIN | UNVERIFIED / do not assume |
| Smart OSM account credential is separate | STRONGLY INDICATED, runtime confirmation pending |
| Local app-lock credential exists | OBSERVED by user; exact storage/relationship unverified |
| ThaiD callback/token routes exist in APK | VERIFIED |
| Exact production ThaiD client configuration | UNVERIFIED |
| Exact post-ThaiD HSS exchange contract | UNVERIFIED |
| Exact API entitlement/role endpoint | UNVERIFIED |

## Implementation rule for Smart_Osm22

Until the runtime contract is verified:

- do not implement a single shared PIN field for all three layers;
- do not use Google Account as the identity source;
- do not use Firebase Auth as the primary identity source;
- do not hardcode a ThaiD client secret in Android;
- keep authentication adapters behind an explicit interface;
- keep ThaiD identity, Smart OSM session, and local unlock state as separate models;
- keep API access tokens in secure platform storage and never in Room/domain entities.

This document is an analysis artifact only. It does not change production authentication behavior.
