# Users API

Spring Boot REST API for managing users, with JWT-based authentication and
role-based access control (RBAC), backed by MySQL via Spring Data JPA and
Flyway migrations.

## Setup

1. Create the database and a dedicated MySQL user:
   ```sql
   CREATE DATABASE users_db;
   CREATE USER 'users_api'@'localhost' IDENTIFIED BY 'YourStrongPassword123';
   GRANT ALL PRIVILEGES ON users_db.* TO 'users_api'@'localhost';
   FLUSH PRIVILEGES;
   ```
2. Copy `.env.example` to `.env` (or edit the included `.env` directly) and
   fill in real values:
   - `DB_USERNAME` / `DB_PASSWORD` - your MySQL credentials
   - `JWT_SECRET` - a long random string (32+ bytes). Generate one with
     `openssl rand -base64 32`.
   - `ADMIN_EMAIL` / `ADMIN_PASSWORD` (optional) - if set, an initial ADMIN
     account is created automatically on first startup, so you have a way
     into the admin-only endpoints. If left blank, no admin is seeded.
3. Open the project in IntelliJ, let Maven import dependencies, and run
   `UsersApiApplication`.
4. Flyway will create/upgrade the `users` table automatically on startup.

## Authentication flow

### Register

```
POST /api/auth/register
Content-Type: application/json

{
  "name": "Alice",
  "email": "alice@example.com",
  "password": "password123",
  "age": 30
}
```

Returns `201 Created` with a JWT:

```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "userId": "...",
  "email": "alice@example.com",
  "role": "USER"
}
```

Self-registration always creates a `USER`-role account. Only an existing
admin can create another admin (via `POST /api/users` with `"role": "ADMIN"`),
or you can seed one via the `ADMIN_EMAIL`/`ADMIN_PASSWORD` env vars described
above.

### Login

```
POST /api/auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "password123"
}
```

Returns the same JWT-bearing response shape as register. Wrong credentials
return `401 Unauthorized`.

### Using the token

Include it on every subsequent request:

```
Authorization: Bearer eyJhbGciOi...
```

Tokens expire after `JWT_EXPIRATION_MS` (default 1 hour, configurable in
`.env`). There is no refresh-token endpoint in this version - once expired,
the client must log in again.

## Routes and access rules

| Route                  | Method            | Access                                  |
|-------------------------|--------------------|------------------------------------------|
| `/api/auth/register`    | POST               | Public                                    |
| `/api/auth/login`       | POST               | Public                                    |
| `/api/profile`          | GET, PUT           | Any authenticated user - own data only    |
| `/api/users`            | GET, POST          | ADMIN only                                |
| `/api/users/{id}`       | GET, PUT, DELETE   | ADMIN only                                |

`/api/profile` always operates on the user identified by the JWT - there is
no `{id}` in the path, so a regular user cannot view or edit anyone else's
profile by manipulating the URL.

`/api/users/**` is restricted to ADMIN both at the URL level (Spring Security
`authorizeHttpRequests`) and again at the method level
(`@PreAuthorize("hasRole('ADMIN')")` on `UserController`), so an admin can
manage any user, including changing roles or deleting accounts.

## Error responses

- `400` - validation failure or malformed request (includes field-level
  `details` for validation errors)
- `401` - missing/invalid/expired JWT, or wrong login credentials
- `403` - valid JWT, but the user's role doesn't permit the action
- `404` - resource not found
- `409` - duplicate email on register or user creation
- `500` - unexpected server error

## Password storage

Passwords are hashed with BCrypt (`BCryptPasswordEncoder`, default strength)
before being persisted. Raw passwords are never stored or logged. The
`UserResponse` DTO is used for all user-returning endpoints specifically so
the password hash is never serialized back to a client.

## Testing in Postman

1. `POST /api/auth/register` with a JSON body of name/email/password/age.
   Copy the `token` from the response.
2. `GET /api/profile` with header `Authorization: Bearer <token>` - returns
   your own profile.
3. `GET /api/users` with that same regular-user token - expect `403`.
4. Log in as the seeded admin (`POST /api/auth/login` with `ADMIN_EMAIL`/
   `ADMIN_PASSWORD`), copy that token, and retry `GET /api/users` - expect
   `200` with the full user list.
