# Task Management API

A RESTful API for task management built with Spring Boot, following **Domain-Driven Design**, **Clean Architecture**, and **Hexagonal Architecture (Ports & Adapters)** principles.

## Tech stack

- Java 21
- Spring Boot 4.1
- Spring Data JPA + Hibernate
- PostgreSQL
- Flyway (database migrations)
- Maven

## Architecture

The project is organized around a single business capability (`task`) divided into three layers:

```
domain/          → Core business logic, no framework dependencies
application/     → Use cases, input/output ports, DTOs
infrastructure/  → Web adapter (REST) and persistence adapter (JPA)
```

Dependencies point inward: `infrastructure` → `application` → `domain`. The domain has no knowledge of any outer layer.

The persistence contract is split across two layers: `TaskRepository` (domain-owned, core CRUD) is extended by `TaskGateway` (application-owned, adds `findAll` with filtering). Services depend on `TaskGateway`; the infrastructure adapter implements it.

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/tasks` | Create a task |
| `GET` | `/api/tasks` | List tasks (optional `?status=` and `?priority=` filters) |
| `PUT` | `/api/tasks/{id}` | Update a task |
| `DELETE` | `/api/tasks/{id}` | Delete a task |
| `PATCH` | `/api/tasks/{id}/start` | Mark a task as in progress |
| `PATCH` | `/api/tasks/{id}/complete` | Mark a task as done |

### Task status values
`TODO` · `IN_PROGRESS` · `DONE`

### Task priority values
`LOW` · `MEDIUM` · `HIGH`

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
  "title": "Fix login bug",
  "description": "Users can't log in with special characters in password",
  "status": "TODO",
  "priority": "HIGH",
  "dueDate": "2025-02-01",
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-01-01T10:00:00"
}
```

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
