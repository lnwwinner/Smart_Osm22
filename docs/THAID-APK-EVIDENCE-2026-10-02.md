# ThaiD APK Evidence — 2026-10-02

This document records additional static evidence recovered directly from the supplied Smart OSM v1.0.8 Android bundle.

## Newly confirmed source/module strings

The Hermes/React Native bundle contains route/module strings including:

- `/thaid/callback.tsx`
- `/consent.tsx`
- `/thaid/mobile/authorize`
- `/thaid/mobile/token`

It also contains configuration-key strings:

- `THAID_PUBLIC_REDIRECT_URI`
- `THAID_REDIRECT_URI`
- `THAID_CLIENT_ID`
- `THAID_PUBLIC_CLIENT_ID`

And authentication/error-state strings:

- `thaid_network_error`
- `thaid_token_exchange_failed`
- `missing_client_id`
- `FIRST_TIME_LOGIN_THIRD_PARTY_ENABLED`

The bundle also contains a third-party exchange route string:

`http://192.168.1.120:8000/api/v1/analytics/volunteers/auth/third-party/exchange`

and the production HSS exchange routes previously observed:

- `https://gw1.hss.moph.go.th/api/exchange/osm/auth/login`
- `https://gw1.hss.moph.go.th/api/exchange/auth/token/refreshAccessToken`

## Interpretation

This strengthens the conclusion that the reference application has an explicit ThaiD callback/configuration layer and a subsequent token-exchange layer.

However, the static bundle does **not** expose the actual production values of the client ID or redirect URI in the strings inspected. It therefore remains unsafe to copy or invent those values.

The local development exchange URL is evidence of a development environment only. It must not be used as the production Smart OSM authentication endpoint.

## What is still unverified

Static extraction has not yet established:

- exact production ThaiD authorization host;
- exact client ID value;
- exact redirect URI value;
- whether PKCE is used;
- authorization request parameters;
- token request body;
- exact ThaiD token response;
- exact provider exchange request/response;
- access-token audience/scope;
- role/permission claims;
- exact Android intent/deep-link configuration.

## Next controlled verification

Use the supplied reference APK in a controlled test environment and capture only metadata from the sign-in transaction:

1. request host/path;
2. HTTP method;
3. non-secret parameter names;
4. redirect/callback URI;
5. response status;
6. token field names;
7. subsequent provider API host/path.

Redact authorization codes, access tokens, refresh tokens, cookies, national IDs, and personal/health data.

Do not bypass TLS validation or install a trust-all interceptor.

## Architecture consequence

Smart_Osm22 should implement an interface such as:

`ThaiDAuthProvider`

with the concrete implementation isolated from the domain layer. Until the exact exchange is verified, the implementation should remain non-production/stubbed rather than guessing provider behavior.
