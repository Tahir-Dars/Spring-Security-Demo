# SpringDemo — Security Overview

This repository demonstrates stateless JWT authentication and role-based authorization in a Spring Boot application. This README focuses on what security is implemented, how it works, how to test it, and quick fixes for common issues (especially around `/signin` and the H2 console).

---

## Quick summary

- Authentication: POST `/signin` accepts credentials and returns a JWT (Bearer token).
- Authorization: protected endpoints require a valid JWT in the `Authorization: Bearer <token>` header.
- Passwords: stored and verified with BCrypt using a shared `PasswordEncoder` bean.
- Filters: custom `AuthTokenFilter` inspects requests for JWTs and sets authentication when valid.
- Error handling: `AuthEntryPoint` returns JSON 401 responses for unauthorized requests.

---

## Key security-related files

- `src/main/java/bank/springdemo/jwt/jwtUtils.java` — create/validate JWTs and extract username.
- `src/main/java/bank/springdemo/jwt/AuthTokenFilter.java` — `OncePerRequestFilter` that reads the `Authorization` header, validates the token, and sets authentication.
- `src/main/java/bank/springdemo/jwt/AuthEntryPoint.java` — returns JSON error responses for 401s.
- `src/main/java/bank/springdemo/jwt/LoginRequest.java` — DTO for signin payload (maps incoming `username` JSON key to the DTO field).
- `src/main/java/bank/springdemo/controller/GreetingController.java` — contains `POST /signin` and example protected endpoints.
- `src/main/java/bank/springdemo/SecurityConfigurations/SecurityConfigs.java` — security configuration (filter chain, `permitAll` paths, `PasswordEncoder`).

---

## Authentication flow (concise)

1. Client POSTs credentials to `POST /signin` (JSON body).
2. Controller authenticates via `AuthenticationManager`.
3. On success the server creates a JWT via `jwtUtils` and returns it as JSON (includes token, username and roles).
4. Client includes `Authorization: Bearer <token>` on subsequent requests to protected endpoints.
5. `AuthTokenFilter` validates the token and sets authentication in the `SecurityContext`.

---

## Important behavior & gotchas (short)

- JSON field binding: the code uses a DTO field `userName` but clients commonly send `username`. The project maps `username` → `userName` using `@JsonProperty`, so sending:

```json
{ "username": "user1", "password": "password1" }
```

with header `Content-Type: application/json` works.

- Auth filter ordering: `AuthTokenFilter` runs early in the filter chain. It should *not* reject a request just because there's no token; it should only populate the `SecurityContext` when a valid token is present. If the filter calls the `AuthenticationEntryPoint` on missing/invalid tokens for all paths, unprotected endpoints such as `/signin` or `/h2-console/**` will be blocked with 401.

- H2 console: the H2 console (`/h2-console`) requires frame support and must be explicitly permitted by your security configuration. If you see a 401 or a JSON error at `/h2-console`, either the endpoint is not permitted or the auth filter rejected the request before `permitAll()` took effect. To allow it, permit `/h2-console/**` and add `http.headers().frameOptions().disable()` in your security config.

---

## Quick test examples

1) Sign in (Postman or curl). Header `Content-Type: application/json`, body:

```json
{
  "username": "user1",
  "password": "password1"
}
```

Curl example:

```powershell
curl -i -H "Content-Type: application/json" -d '{"username":"user1","password":"password1"}' http://localhost:8080/signin
```

2) Use returned token to call a protected endpoint:

```powershell
curl -i -H "Authorization: Bearer <TOKEN>" http://localhost:8080/user/hello
```

3) H2 console: open `http://localhost:8080/h2-console` after ensuring security config permits it. If the console shows a 401 JSON, check the filter behavior and security config.

---

## Common problems & fixes (short)

- Bad Credentials / 401 on `/signin`:
  - Cause: request JSON not bound (field name mismatch) or `Content-Type` missing. Fix: send `Content-Type: application/json` and keys matching the DTO (this project maps `username` to the DTO field).
  - Cause: `AuthenticationManager` cannot find the user or password encoder mismatch. Fix: ensure seeded users exist and use BCrypt.

- 401 on `/h2-console`:
  - Cause: `AuthTokenFilter` blocked the request before `permitAll()` applied. Fix: make the filter ignore requests with no Authorization header (i.e., skip token parsing when header is absent) or explicitly permit `/h2-console/**` and disable frame options in `SecurityConfigs`.

- Token validation errors:
  - Cause: wrong signing key, malformed token, expired token. Fix: check `spring.app.jwtSecret` and `spring.app.jwtExpirations_ms` in `application.properties` and inspect logs from `jwtUtils`.

---

## Minimal recommended fixes (if you encounter issues)

- Ensure DTO binding: map `username` JSON key to your DTO (already done here using `@JsonProperty`).
- Make `AuthTokenFilter` tolerant of no-token requests: only set `SecurityContext` when a valid token is present; do not call `AuthEntryPoint` for absent tokens on unprotected endpoints.
- In `SecurityConfigs`, explicitly `permitAll()` for `POST /signin` and `"/h2-console/**"` and disable `frameOptions`.
- Use a single `PasswordEncoder` bean (BCrypt) everywhere (user seeding + authentication).

---

## Where to look in code (first steps when debugging)

1. `src/main/java/bank/springdemo/jwt/AuthTokenFilter.java` — see how the token is parsed and whether missing tokens cause immediate errors.
2. `src/main/java/bank/springdemo/jwt/AuthEntryPoint.java` — JSON structure and messages for 401 responses.
3. `src/main/java/bank/springdemo/jwt/jwtUtils.java` — signing/verification logic.
4. `src/main/java/bank/springdemo/SecurityConfigurations/SecurityConfigs.java` — permitted paths and filter chain ordering.
5. `src/main/java/bank/springdemo/controller/GreetingController.java` — the `POST /signin` implementation.


