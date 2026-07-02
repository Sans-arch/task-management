# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Spring Boot 4.1 / Java 21 REST API for task management, built with Maven. The project is in early
scaffolding: most classes exist as empty stubs (e.g. `CreateTaskUseCase`, `TaskController`,
`TaskMapper`) awaiting implementation, while a few slices (`Task` domain model, `UpdateTaskUseCase`
→ `UpdateTaskService`) are fully wired end-to-end. Use those wired slices as the reference pattern
when filling in a stub.

Note: the intended package name `com.github.sansarch.task-management` is not a valid Java
identifier, so the codebase uses `com.github.sansarch.task_management` (underscore) instead.

## Git conventions

- Always commit as the configured git user (`Sans-arch`).
- Never add `Co-Authored-By: Claude` or any other Claude authorship line to commits.

## Commands

Use the Maven wrapper (`./mvnw`), not a system-installed `mvn`.

- Build: `./mvnw compile`
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=TaskTest`
- Run a single test method: `./mvnw test -Dtest=TaskTest#shouldCreateTask`
- Run the app locally: `./mvnw spring-boot:run`
- Package: `./mvnw package`

## Architecture

The codebase follows **hexagonal architecture (ports & adapters)**, applying **Clean
Architecture**'s dependency rule (dependencies point inward, domain at the center) together with
**Domain-Driven Design** tactical patterns (entities, value objects, domain exceptions) inside the
`domain` layer. It's organized per business capability (currently just `task`). Each layer lives
under its own top-level package and only depends inward:

```
domain/task/
  model/       Task, TaskId, TaskStatus, TaskPriority — no framework dependencies
  exception/   InvalidTaskStateException, TaskNotFoundException (RuntimeException)

application/task/
  port/in/     Use case interfaces called by inbound adapters (e.g. UpdateTaskUseCase)
  port/out/    Repository interfaces implemented by outbound adapters (e.g. TaskRepository)
  service/     Use case implementations (@Service), e.g. UpdateTaskService implements UpdateTaskUseCase
  dto/         Commands/Results/Filters crossing the application boundary (e.g. UpdateTaskCommand, TaskResult)

infrastructure/task/
  adapter/in/web/          TaskController (@RestController), request/response DTOs
  adapter/out/persistence/ SpringDataTaskRepository (Spring Data JPA), TaskJpaEntity,
                            TaskMapper (domain <-> JPA entity), TaskRepositoryAdapter
                            (implements the application's TaskRepository port)
```

Dependency rule: `domain` has no dependencies on other layers; `application` depends only on
`domain`; `infrastructure` depends on `application` and `domain`. Inbound adapters (web) call
`port/in` use-case interfaces; use-case services depend on `port/out` interfaces, which outbound
adapters (persistence) implement — never the other way around.

### Domain model conventions (DDD tactical patterns, see `Task.java`)

- `Task` is a mutable entity with private fields, validated in a `validate()` method called from
  every constructor and every state-mutating method (`update`, `markInProgress`, `complete`).
- Invalid state throws `InvalidTaskStateException`; state transitions are guarded (e.g.
  `markInProgress()` only allowed from `TODO`, `complete()` rejects an already-`DONE` task).
- `TaskId` wraps a `UUID` in a record; `TaskId.generate()` creates a new random id. Equality on
  `Task` is by id only.
- `updatedAt` is refreshed on every mutation; `validate()` also enforces `updatedAt` is never
  before `createdAt`.

### Application layer conventions

- Use-case interfaces in `port/in` are implemented by a single `@Service` in `service/` (one
  service per use case, e.g. `UpdateTaskService`).
- Services take `Command` DTOs in and return `Result` DTOs out (see `UpdateTaskCommand` /
  `TaskResult`); they never expose domain objects (`Task`) across the application boundary.
- Services depend on `port/out` interfaces (e.g. `TaskRepository`), injected via constructor —
  never on a concrete persistence adapter.
- Not-found lookups throw `TaskNotFoundException`.

## Testing conventions

- Every use case service (`application/task/service/`) must have a corresponding unit test class
  under `src/test/.../application/task/service/`.
- Use Mockito to mock `TaskRepository` (and any other ports); never spin up a Spring context for
  unit tests.
- Use JUnit 5 + AssertJ: `@Nested` classes grouped by method under test, `@DisplayName` on every
  class and test method.
- Never call `LocalDate.now()` or `LocalDateTime.now()` in tests — use fixed constants instead
  (SonarLint: "Do not use the system clock in tests").
- Domain entity tests live under `src/test/.../domain/task/model/` (see `TaskTest`).

## Stack

- Spring Boot starters: `data-jpa`, `validation`, `webmvc` (+ matching `-test` starters for
  Boot's test slices), plus `devtools` and the `postgresql` runtime driver.
- No `pom.xml` changes to the parent POM's empty `<license>`/`<developers>`/etc. overrides —
  these are intentional (see `HELP.md`) to prevent inheriting unwanted values from
  `spring-boot-starter-parent`.
