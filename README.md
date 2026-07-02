# Task Management API

A RESTful API for task management built with Spring Boot, following **Domain-Driven Design**, **Clean Architecture**, and **Hexagonal Architecture (Ports & Adapters)** principles.

## Tech stack

- Java 21
- Spring Boot 4.1
- Spring Data JPA
- PostgreSQL
- Maven

## Architecture

The project is organized around a single business capability (`task`) divided into three layers:

```
domain/          → Core business logic, no framework dependencies
application/     → Use cases, input/output ports, DTOs
infrastructure/  → Web adapter (REST) and persistence adapter (JPA)
```

Dependencies point inward: `infrastructure` → `application` → `domain`. The domain has no knowledge of any outer layer.

## API endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/tasks` | Create a task |
| `GET` | `/api/tasks` | List tasks (optional `?status=` and `?priority=` filters) |
| `PUT` | `/api/tasks/{id}` | Update a task |
| `DELETE` | `/api/tasks/{id}` | Delete a task |

### Task status values
`TODO` · `IN_PROGRESS` · `DONE`

### Task priority values
`LOW` · `MEDIUM` · `HIGH`

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
- A running PostgreSQL instance

### Configuration

Set the following environment variables (or override them in `application.properties`):

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/task_management
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=your_password
```

### Start the application

```bash
./mvnw spring-boot:run
```

## Running tests

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
