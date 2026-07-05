# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Spring Boot 4.1 / Java 21 REST API for task management, built with Maven. The API supports
multiple users: tasks belong to an owner, users can organize into groups, and group members get
full shared management (view/edit/delete) over each other's tasks. Authentication is a hand-rolled
JWT mechanism (login issues a token, a servlet filter validates it) — deliberately simple for now;
see "Security & auth conventions" below for how a future OAuth2 phase slots in without touching
`domain`/`application`. Three business capabilities exist today — `task`, `user`, `group` — each
following the same hexagonal layering; use any of them as the reference pattern when adding a new
one.

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
`domain` layer. It's organized per business capability (`task`, `user`, `group`, plus a thin `auth`
capability with no domain layer of its own — see below). Each layer lives under its own top-level
package and only depends inward:

```
domain/task/
  model/       Task, TaskId, TaskStatus, TaskPriority — no framework dependencies
  exception/   InvalidTaskStateException, TaskNotFoundException (RuntimeException)
  repository/  TaskRepository — core persistence contract owned by the domain

application/task/
  port/in/     Use case interfaces called by inbound adapters (e.g. UpdateTaskUseCase)
  port/out/    TaskGateway — extends TaskRepository, adds application-level queries (findAll)
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

### Repository and gateway conventions

The persistence contract is split across two layers:

- **`domain/task/repository/TaskRepository`** — owned by the domain. Defines only the core
  persistence operations the domain needs: `findById`, `save`, `delete`. No application DTOs.
- **`application/task/port/out/TaskGateway`** — output port owned by the application layer.
  Extends `TaskRepository` and adds application-specific queries (e.g. `findAll(TaskFilter)`).
  All services depend on `TaskGateway`, never on `TaskRepository` directly.
- **`infrastructure/.../TaskRepositoryAdapter`** — implements `TaskGateway` (and transitively
  `TaskRepository`). Never referenced by domain or application code.

### Application layer conventions

- Use-case interfaces in `port/in` are implemented by a single `@Service` in `service/` (one
  service per use case, e.g. `UpdateTaskService`).
- Services take `Command` DTOs in and return `Result` DTOs out (see `UpdateTaskCommand` /
  `TaskResult`); they never expose domain objects (`Task`) across the application boundary.
- Services depend on `port/out` interfaces (e.g. `TaskRepository`), injected via constructor —
  never on a concrete persistence adapter.
- Not-found lookups throw `TaskNotFoundException`.

## Security & auth conventions

- **`infrastructure/auth/`** is a top-level package (sibling to `infrastructure/task/`,
  `infrastructure/user/`, `infrastructure/group/`) holding `SecurityConfig`, the JWT
  service/filter, security entry points, and `AuthController`. It has no `domain/auth` layer —
  `User` already owns the identity concept — so `application/auth` is deliberately thin
  (`LoginUseCase` + `TokenIssuer` port only).
- **Current-user access**: application services depend on
  `application/shared/port/out/AuthenticatedUserProvider` (`UserId getCurrentUserId()`), never on
  Spring Security types directly. It's implemented by
  `infrastructure/auth/security/AuthenticatedUserProviderImpl`, which reads
  `SecurityContextHolder` — the `JwtAuthenticationFilter` sets the principal to the `UserId` value
  object itself (not a String), which is what makes the provider trivially type-safe. Controllers
  never touch this port directly; only application services do.
- **Password hashing** is its own port, `application/user/port/out/PasswordHasher`, implemented by
  `infrastructure/user/adapter/out/security/BCryptPasswordHasher` wrapping a Spring
  `PasswordEncoder` bean — keeps `application/user` framework-agnostic rather than injecting
  `PasswordEncoder` straight into a service.
- **`TaskAuthorizationService`** (`application/task/security/`) is a documented deviation from
  "one service per use-case interface": it's a plain `@Service` collaborator, not a `port/in`
  implementation, shared by `UpdateTaskService`, `DeleteTaskService`, `MarkTaskInProgressService`,
  `CompleteTaskService`, and `ListTasksService` to avoid duplicating the "can this caller manage
  this task" check five times. `manageableOwnerIds()` returns the current user plus their group
  co-members (via `GroupMembershipGateway.findCoMemberIds`); `assertCanManage(Task)` throws
  `TaskAccessDeniedException` (403) otherwise. When adding a new task use case, inject this
  service and call `assertCanManage` right after the `findById(...).orElseThrow(...)` lookup.
- **Never trust a client-supplied owner/user id.** `CreateTaskCommand` has no `ownerId` field —
  `CreateTaskService` stamps it from `AuthenticatedUserProvider`. Apply the same rule to any future
  command that would otherwise let a client impersonate another user.
- **Login timing/enumeration**: `LoginService` must throw the *identical* exception and message
  (`InvalidCredentialsException("Invalid email or password")`) whether the email is unknown or the
  password is wrong — never let the two failure branches diverge.
- **OAuth2 slot-in note**: because `AuthenticatedUserProvider` is the only thing application
  services depend on for "who is calling," and the filter chain lives in one `SecurityConfig`
  class, a future phase can add (or swap in) a `spring-boot-starter-oauth2-resource-server` filter
  chain without touching `domain`, `application`, or any `task`/`user`/`group` controller — only
  `infrastructure/auth` changes. This is deliberately not built yet.

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
- Naming distinguishes unit tests from integration tests: plain unit tests (no Spring context)
  use the `*Test` suffix (e.g. `UpdateTaskServiceTest`, `TaskTest`); tests that spin up a Spring
  context use the `*IT` suffix (e.g. `TaskControllerIT`, `TaskRepositoryAdapterIT`). Both run via
  `./mvnw test` — `pom.xml` widens Surefire's default includes to pick up `*IT.java` too, since
  there's no separate Failsafe/`verify` phase in this project.
- Whenever it's convenient — e.g. when adding or changing a web controller or a persistence
  adapter — add or update the matching integration test alongside the unit tests: `@WebMvcTest`
  for controllers (see `TaskControllerIT`), `@DataJpaTest` + Testcontainers for persistence
  adapters (see `TaskRepositoryAdapterIT`).

## Stack

- Spring Boot starters: `data-jpa`, `validation`, `webmvc`, `security` (+ matching `-test` starters
  for Boot's test slices, plus plain `spring-security-test`), `devtools`, and the `postgresql`
  runtime driver.
- JWT: `jjwt-api`/`jjwt-impl`/`jjwt-gson` — `jjwt-gson`, not `jjwt-jackson`, deliberately, since
  this codebase runs Jackson 3 (`tools.jackson.*`) and `jjwt-jackson` targets classic
  `com.fasterxml.jackson`.
- No `pom.xml` changes to the parent POM's empty `<license>`/`<developers>`/etc. overrides —
  these are intentional (see `HELP.md`) to prevent inheriting unwanted values from
  `spring-boot-starter-parent`.
