# SpringDemo — Security Overview

This repository is a Spring Boot demo focused on implementing stateless JWT authentication and role-based authorization. The README explains what security features are implemented, why they are used, how the main pieces interact, and how to test them (including H2 console access).

---

## Quick summary

- Authentication: POST `/signin` accepts credentials and returns a JWT (Bearer token).
- Authorization: protected endpoints require a valid JWT in the `Authorization: Bearer <token>` header.
- Passwords: stored/verified with BCrypt via a shared `PasswordEncoder`.
- Filters: a custom `AuthTokenFilter` extracts & validates JWTs and populates the `SecurityContext`.
- Error handling: `AuthEntryPoint` returns JSON 401 responses for unauthorized requests.

---

## Key security-related files

- `src/main/java/bank/springdemo/jwt/jwtUtils.java` — create/validate JWTs and extract username.
- `src/main/java/bank/springdemo/jwt/AuthTokenFilter.java` — Spring `OncePerRequestFilter` that reads Authorization header, validates token, and sets authentication.
- `src/main/java/bank/springdemo/jwt/AuthEntryPoint.java` — returns JSON error responses for 401s.
- `src/main/java/bank/springdemo/jwt/LoginRequest.java` — DTO for signin payload.
- `src/main/java/bank/springdemo/controller/GreetingController.java` — contains `POST /signin` and example protected endpoints.
- `src/main/java/bank/springdemo/SecurityConfigurations/SecurityConfigs.java` — security configuration (filter chain, `permitAll` paths, `PasswordEncoder`).

---

## Authentication flow (concise)

1. Client POSTs credentials to `POST /signin` (JSON body).
2. Controller authenticates via `AuthenticationManager`.
3. On success the server creates a JWT via `jwtUtils` and returns it as JSON.
4. Client includes `Authorization: Bearer <token>` on subsequent requests to protected endpoints.
5. `AuthTokenFilter` validates the token and sets authentication in the `SecurityContext`.

---

## Important behavior & gotchas (short)

- JSON field binding: the signin DTO uses `userName` in code but clients commonly send `username`. The project maps the JSON key `username` to the DTO field so sending `{ "username": "user1", "password": "password1" }` with `Content-Type: application/json` works.

- Auth filter ordering: `AuthTokenFilter` runs before core authentication filters. If it treats a missing/invalid token as an immediate error (calling the `AuthenticationEntryPoint`), requests like `/signin` or `/h2-console/**` may be blocked before the controller or security config `permitAll()` is applied. The recommended behavior is to *skip* token validation when no token is present, and only populate the `SecurityContext` when a valid token exists.

- H2 console: the H2 console (`/h2-console`) is a web UI that requires frame support and must be permitted in the security configuration. If you see a 401 for `/h2-console`, it means access is blocked by security (or the filter rejected the request due to missing/invalid token). To allow it, ensure `SecurityConfigs` permits `/h2-console/**` and call `http.headers().frameOptions().disable()`.

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

```bash
curl -i -H "Content-Type: application/json" -d '{"username":"user1","password":"password1"}' http://localhost:8080/signin
```

2) Use returned token to call a protected endpoint:

```bash
curl -i -H "Authorization: Bearer <TOKEN>" http://localhost:8080/user/hello
```

3) H2 console: open `http://localhost:8080/h2-console` after ensuring security config permits it. If the console shows a 401 JSON, check the filter behavior and security config.

---

## Common problems & fixes (short)

- Bad Credentials / 401 on `/signin`:
  - Cause: request JSON was not bound (field name mismatch) or `Content-Type` missing. Fix: send `Content-Type: application/json` and keys matching the DTO (the repo maps `username` → `userName`).
  - Cause: `AuthenticationManager` cannot find the user or password encoding mismatch. Fix: ensure seeded users exist and use BCrypt.

- 401 on `/h2-console`:
  - Cause: `AuthTokenFilter` blocked the request before `permitAll()` applied. Fix: make the filter ignore requests with no Authorization header, or explicitly permit `/h2-console/**` in `SecurityConfigs` and disable frameOptions.

- Token validation errors:
  - Cause: wrong signing key, malformed token, expired token. Fix: check `spring.app.jwtSecret` and `spring.app.jwtExpirations_ms` in `application.properties` and inspect logs from `jwtUtils`.

---

## Minimal recommended fixes (if you encounter issues)

- Ensure DTO binding: map `username` JSON key to your DTO (already done here using `@JsonProperty`).
- Make `AuthTokenFilter` tolerant of no-token requests: only set SecurityContext when a valid token is present; do not trigger an immediate 401 from the filter.
- In `SecurityConfigs`, explicitly `permitAll()` for `POST /signin` and `"/h2-console/**"` and disable `frameOptions`.
- Use a single `PasswordEncoder` bean (BCrypt) everywhere (user seeding + authentication).

---

## Where to look in code (first steps when debugging)

1. `src/main/java/bank/springdemo/jwt/AuthTokenFilter.java` — see how the token is parsed and whether missing tokens cause immediate errors.
2. `src/main/java/bank/springdemo/jwt/AuthEntryPoint.java` — JSON structure and messages for 401 responses.
3. `src/main/java/bank/springdemo/jwt/jwtUtils.java` — signing/verification logic.
4. `src/main/java/bank/springdemo/SecurityConfigurations/SecurityConfigs.java` — permitted paths and filter chain ordering.
5. `src/main/java/bank/springdemo/controller/GreetingController.java` — the `POST /signin` implementation.

---

## Final notes

This README focuses on the security design used in the project and practical steps to test and troubleshoot it. If you want, I can:

- Add a short `SECURITY.md` with more details (threat model and mitigations). 
- Make the `AuthTokenFilter` explicitly skip unprotected endpoints (I can apply that change).

If you'd like either of those, tell me which and I'll implement it.

