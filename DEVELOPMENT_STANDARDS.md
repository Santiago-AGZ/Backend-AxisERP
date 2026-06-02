# DEVELOPMENT_STANDARDS.md

# AxisERP Development Standards

## Quality Standard

Every implementation must satisfy 100% of the defined requirements.

A feature is not considered complete if:

* Business rules are missing.
* Validations are missing.
* Security controls are missing.
* Audit requirements are missing.
* Critical tests are missing.
* Documentation is missing.
* Domain events are missing when required.
* API contracts are incomplete.

---

## Development Workflow

Before implementing any change:

1. Analyze the requirement.
2. Review BUSINESS_RULES.md.
3. Identify affected business rules.
4. Identify affected microservices.
5. Review existing implementation.
6. Review existing tests.
7. Apply required skills.
8. Run Superpowers analysis when available.
9. Create implementation plan.
10. Explain implementation impact.

Code generation must occur only after analysis and planning.

---

## Mandatory Skills

### Architecture

* clean-ddd-hexagonal
* hexagonal-architecture
* caveman
* composition-patterns
* impeccable

### Microservices

* microservices-architect
* microservices-patterns

### Backend

* java-coding-standards
* java-docs
* java-springboot
* springboot-patterns

### API Design

* rest-api-design
* spring-boot-rest-api-standards
* postman-collection-generator

### Security

* better-auth-best-practices
* better-auth-security-best-practices
* owasp-security

### Database

* neon-postgres
* supabase-postgres-best-practices

### Infrastructure

* docker-containerization
* docker-compose-orchestration
* docker-expert
* docker-patterns
* multi-stage-dockerfile

---

## Architecture Standards

All implementations must respect:

* DDD
* Hexagonal Architecture
* Clean Architecture
* SOLID Principles
* Separation of Concerns
* Single Responsibility Principle

The domain layer must not depend on infrastructure.

Business rules must remain inside the domain.

---

## Java Standards

Required:

* Java 21
* Constructor Injection
* Immutable DTOs when possible
* Use records when appropriate
* Avoid field injection
* Avoid static business logic
* Avoid God classes

Code must remain readable, maintainable and testable.

---

## Spring Boot Standards

Required:

* Spring Boot 3.5.x
* Layered package structure
* Global exception handling
* Validation using Bean Validation
* Configuration through properties
* Environment-based configuration

Forbidden:

* Business logic inside controllers.
* Business logic inside repositories.
* Direct infrastructure dependencies inside domain layer.

---

## API Standards

All APIs must:

* Follow REST conventions.
* Use resource-oriented naming.
* Use proper HTTP methods.
* Use proper HTTP status codes.
* Return standardized responses.
* Document all endpoints.
* Document all DTOs.
* Document all error responses.

Required:

* Pagination
* Filtering
* Sorting when applicable

---

## API Response Standard

All services must expose a standardized response structure.

Example:

```json
{
  "success": true,
  "code": "OK",
  "message": "Operation completed",
  "data": {},
  "errors": [],
  "meta": {},
  "pagination": {}
}
```

---

## Documentation Standards

Required:

* OpenAPI
* Swagger UI (Development)
* Automatic API documentation

Documentation must remain synchronized with implementation.

All public APIs must be documented.

---

## Security Standards

Required:

* OWASP practices
* JWT validation
* Authorization checks
* Audit logging
* Secure password handling
* Input validation

Forbidden:

* Plain text passwords
* Hardcoded secrets
* Exposed credentials
* Security bypasses

---

## Audit Standards

All critical operations must be audited.

Audit records must include:

* User
* Action
* Date
* Entity
* Entity Identifier
* IP when applicable

Audit records must be immutable.

---

## Testing Standards

Mandatory:

* JUnit 5
* Mockito
* MockMvc

Required:

* Unit tests
* Integration tests when applicable

Critical business flows must be tested.

Business rules must be tested.

State transitions must be tested.

Security validations must be tested.

---

## Database Standards

Required:

* PostgreSQL
* Neon PostgreSQL

Architecture:

* Database-per-Service

Forbidden:

* Shared databases
* Cross-service joins
* Direct database access between services

Each service owns its data.

---

## RabbitMQ Standards

RabbitMQ must be used for domain events.

Events must:

* Be immutable.
* Represent completed business actions.
* Be versionable.
* Be traceable.

Examples:

* UsuarioCreado
* CompraRecibida
* VentaConfirmada
* StockActualizado
* FacturaGenerada

---

## Docker Standards

Required:

* Dockerfile per service
* Docker Compose
* Environment variables
* Multi-stage builds when applicable

Containers must be reproducible and portable.

---

## Git Strategy

Branches:

* main
* feat/*
* fix/*
* hotfix/*

Commits must be focused and atomic.

---

## Business Rules

Business rules always have higher priority than implementation convenience.

The system must never violate a business rule to simplify development.

All business rules are defined in BUSINESS_RULES.md.

---

## Communication Rules

Allowed communication mechanisms:

### Synchronous

* REST APIs

### Asynchronous

* RabbitMQ Domain Events

Forbidden:

* Direct database integration between services.
* Shared schemas.
* Shared tables.

---

## Definition of Done

A feature is considered complete only when:

* Requirements are implemented.
* Business rules are implemented.
* Tests are implemented.
* Documentation is updated.
* Security requirements are satisfied.
* Audit requirements are satisfied.
* Architecture standards are respected.
* Code review issues are resolved.
* The solution is production-ready.

```
```
