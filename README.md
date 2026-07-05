# Task Management API

A RESTful API for task management built with Spring Boot, following **Domain-Driven Design**, **Clean Architecture**, and **Hexagonal Architecture (Ports & Adapters)** principles.

Tasks belong to a user; users can organize into groups; group members get full shared management (view/edit/delete) over each other's tasks. Authentication is a simple, hand-rolled JWT mechanism — deliberately kept simple for now, with real OAuth2 planned as a later phase.

## Tech stack

- Java 21
- Spring Boot 4.1
- Spring Security + JJWT (JWT issuing/validation)
- Spring Data JPA + Hibernate
- PostgreSQL
- Flyway (database migrations)
- Maven

## Architecture

The project is organized per business capability (`task`, `user`, `group`, plus a thin `auth` capability), each divided into three layers:

```
domain/          → Core business logic, no framework dependencies
application/     → Use cases, input/output ports, DTOs
infrastructure/  → Web adapter (REST) and persistence adapter (JPA)
```

Dependencies point inward: `infrastructure` → `application` → `domain`. The domain has no knowledge of any outer layer.

The persistence contract is split across two layers: a `Repository` (domain-owned, core CRUD) is extended by a `Gateway` (application-owned, adds queries needed by use cases). Services depend on the gateway; the infrastructure adapter implements it.

Task authorization (who can view/edit/delete a task) is centralized in `TaskAuthorizationService`: a task's manageable owners are its owner plus anyone sharing a group with them. See `CLAUDE.md` for the full set of security/auth conventions.

## API endpoints

### Users & auth

| Method | Endpoint | Auth required | Description |
|--------|----------|:---:|-------------|
| `POST` | `/api/users` | no | Register a new user |
| `GET` | `/api/users/me` | yes | Get the currently authenticated user |
| `POST` | `/api/auth/login` | no | Log in, returns a Bearer JWT |

### Groups

| Method | Endpoint | Auth required | Description |
|--------|----------|:---:|-------------|
| `POST` | `/api/groups` | yes | Create a group (creator auto-joins) |
| `GET` | `/api/groups` | yes | List the groups you belong to |
| `POST` | `/api/groups/{id}/members` | yes | Add a member by email (requester must already be a member) |
| `DELETE` | `/api/groups/{id}/members/{userId}` | yes | Remove a member (or leave, by removing yourself) |

### Tasks

All task endpoints require authentication. A task is only manageable by its owner or a user sharing a group with the owner; `GET /api/tasks` is scoped accordingly.

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/tasks` | Create a task (owned by the caller) |
| `GET` | `/api/tasks` | List tasks you can manage, paginated (optional `?status=`, `?priority=`, `?page=`, `?size=`, `?sortBy=`, `?sortDirection=`) |
| `PUT` | `/api/tasks/{id}` | Update a task |
| `DELETE` | `/api/tasks/{id}` | Delete a task |
| `PATCH` | `/api/tasks/{id}/start` | Mark a task as in progress |
| `PATCH` | `/api/tasks/{id}/complete` | Mark a task as done |

### Task status values
`TODO` · `IN_PROGRESS` · `DONE`

### Task priority values
`LOW` · `MEDIUM` · `HIGH`

### Pagination and sorting

`GET /api/tasks` returns a page of results:

| Param | Default | Notes |
|-------|---------|-------|
| `page` | `0` | Zero-based page index |
| `size` | `20` | Max `100` |
| `sortBy` | `CREATED_AT` | One of `TITLE`, `STATUS`, `PRIORITY`, `DUE_DATE`, `CREATED_AT`, `UPDATED_AT` |
| `sortDirection` | `DESC` | `ASC` or `DESC` |

```json
{
  "content": [ { "id": "...", "title": "...", "...": "..." } ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

### State transitions

```
TODO → IN_PROGRESS  (via PATCH /start)
TODO → DONE         (via PATCH /complete)
IN_PROGRESS → DONE  (via PATCH /complete)
```

### Example request body (create / update)

```json
{
  "title": "Fix login bug",
  "description": "Users can't log in with special characters in password",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-02-01"
}
```

### Example response

```json
{
  "id": "a3f1c2d4-...",
  "ownerId": "b7e2a9f0-...",
  "title": "Fix login bug",
  "description": "Users can't log in with special characters in password",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-02-01",
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-01-01T10:00:00"
}
```

## Authentication

Register, log in, and use the returned token as a `Bearer` header on every subsequent request:

```bash
# Register
curl -X POST localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"password123","displayName":"Jane Doe"}'

# Log in
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"password123"}' | python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

# Create a task as the authenticated user
curl -X POST localhost:8080/api/tasks \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Fix login bug","status":"TODO","priority":"HIGH"}'
```

Tokens are HMAC-signed JWTs, valid for 60 minutes by default (`app.security.jwt.expiration-minutes`).

## Running locally

### Prerequisites

- Java 21
- Docker

### Start the application

```bash
# Start PostgreSQL
docker compose up -d

# Run the application
./mvnw spring-boot:run
```

Flyway runs automatically on startup and applies any pending migrations from `src/main/resources/db/migration/`.

The default datasource in `application.yaml` points to the Compose database (`localhost:5432/task_management`). Override with environment variables if you use a different instance:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/task_management
SPRING_DATASOURCE_USERNAME=task_user
SPRING_DATASOURCE_PASSWORD=task_password
```

### JWT secret

`app.security.jwt.secret` defaults to a dev-only value and can be overridden with the `JWT_SECRET` environment variable. It must be at least 32 bytes (HMAC-SHA256 requirement) — never use the default outside local development:

```
JWT_SECRET=a-properly-random-secret-at-least-32-bytes-long
```

### Upgrading an existing local database

Migration `V3` adds a `NOT NULL owner_id` column to `tasks`. If you have pre-existing local data from before this migration (rows with no owner), wipe the Compose volume and start clean rather than trying to migrate it in place:

```bash
docker compose down -v
docker compose up -d
```

## Running tests

Unit tests (domain + application layer) run without any infrastructure. Persistence integration tests spin up a PostgreSQL container automatically via Testcontainers — Docker must be running.

```bash
# All tests
./mvnw test

# Single class
./mvnw test -Dtest=TaskTest

# Single method
./mvnw test -Dtest=TaskTest#shouldCreateTask
```

## Building

```bash
./mvnw package
```
