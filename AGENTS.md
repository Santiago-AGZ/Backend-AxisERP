# AGENTS.md

# AxisERP Development Agent Instructions

## Project Overview

AxisERP is an ERP platform built using:

* Java 21
* Spring Boot 3.5.x
* PostgreSQL (Neon)
* RabbitMQ
* Docker
* Maven

Architecture:

* Domain Driven Design (DDD)
* Hexagonal Architecture
* Clean Architecture
* Microservices Architecture

---

## Existing Microservices

* api-gateway
* auth-service
* catalog-service
* inventory-service
* purchase-service
* sales-service
* report-service

---

## Source of Truth

The agent must always consult:

* BUSINESS_RULES.md
* ARCHITECTURE.md
* DEVELOPMENT_STANDARDS.md
* MICROSERVICE_INTERACTIONS.md

Business rules are mandatory.

Business rules have higher priority than implementation convenience.

No implementation may violate a business rule.

---

## Mandatory Workflow

Before writing any code:

1. Analyze the requirement.
2. Review BUSINESS_RULES.md.
3. Review ARCHITECTURE.md.
4. Review DEVELOPMENT_STANDARDS.md.
5. Review MICROSERVICE_INTERACTIONS.md.
6. Identify affected business rules.
7. Identify affected microservices.
8. Identify affected entities.
9. Review existing architecture.
10. Review existing implementation.
11. Review existing tests.
12. Identify applicable skills.
13. Apply mandatory skills.
14. Run Superpowers analysis.
15. Create implementation plan.
16. Explain implementation plan.
17. Explain technical impact.
18. Explain affected business rules.
19. Explain affected APIs.
20. Explain affected database structures.

Only after completing the previous steps may code be generated.

---

## Superpowers Rules

Superpowers analysis is mandatory before implementing code.

The agent must use Superpowers to:

* Understand the existing codebase.
* Identify existing implementations.
* Detect reusable components.
* Detect duplicated logic.
* Understand architecture decisions.
* Understand service boundaries.
* Validate implementation approach.
* Identify potential side effects.

The agent must not generate code before completing Superpowers analysis.

If Superpowers is unavailable, the agent must manually perform equivalent analysis before implementation.

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

## Skills Usage Rules

Before implementing any change, the agent must identify and apply all relevant skills.

The agent must not start implementation until the applicable skills have been identified.

Multiple skills may be combined when required.

Examples:

### New Spring Boot Endpoint

Required skills:

* java-springboot
* spring-boot-rest-api-standards
* rest-api-design

### Authentication Feature

Required skills:

* better-auth-best-practices
* better-auth-security-best-practices
* owasp-security

### New Microservice Integration

Required skills:

* microservices-architect
* microservices-patterns

### Database Design

Required skills:

* neon-postgres
* supabase-postgres-best-practices

### Docker Deployment

Required skills:

* docker-containerization
* docker-compose-orchestration
* multi-stage-dockerfile

The agent must explain which skills are being applied before implementation.

---

## Planning Rules

The agent must always:

1. Analyze.
2. Create a plan.
3. Explain the plan.
4. Identify affected business rules.
5. Identify affected services.
6. Identify affected APIs.
7. Identify affected databases.
8. Identify affected domain events.
9. Identify affected tests.
10. Implement.

The agent must not immediately generate large amounts of code without first presenting analysis and implementation plan.

---

## Architecture Rules

The agent must always:

* Respect DDD.
* Respect Hexagonal Architecture.
* Respect Clean Architecture.
* Respect SOLID principles.
* Respect OWASP security practices.
* Respect microservice boundaries.
* Respect Database-per-Service.
* Respect RabbitMQ domain-event architecture.
* Respect ownership of data by each service.
* Respect eventual consistency principles.
* Respect domain ownership.

The agent must never:

* Access another service database.
* Create joins between databases.
* Create direct dependencies between domains.
* Bypass service boundaries.
* Duplicate business logic unnecessarily.
* Create distributed transactions between services.
* Create shared schemas.
* Create shared tables.

---

## Communication Rules

Allowed communication mechanisms:

### Synchronous

* REST APIs

### Asynchronous

* RabbitMQ Domain Events

Examples:

* UsuarioCreado
* UsuarioActualizado
* PasswordCambiada
* ProductoCreado
* ProductoActualizado
* ProductoEliminado
* CompraRecibida
* CompraAnulada
* VentaConfirmada
* VentaAnulada
* StockActualizado
* StockBajo
* StockAgotado
* FacturaGenerada

Forbidden:

* Direct database communication.
* Shared database integration.
* Cross-service joins.

---

## Business Rules Enforcement

Every implementation must:

* Respect all business rules.
* Respect all validations.
* Respect state transitions.
* Respect audit requirements.
* Respect security requirements.
* Respect authorization requirements.
* Respect domain invariants.

No feature is considered complete if business rules are missing.

Business rules defined in BUSINESS_RULES.md are mandatory.

---

## Testing Rules

Every implementation must include:

* JUnit 5 tests.
* Mockito tests.
* MockMvc tests when applicable.

Required testing:

* Unit tests.
* Integration tests when applicable.
* Business rule validation tests.
* State transition tests.
* Security validation tests.

Critical business flows must be tested.

The agent must update tests whenever behavior changes.

A feature is not complete without appropriate tests.

---

## Documentation Rules

All APIs must provide:

* OpenAPI documentation.
* Swagger UI in development environments.
* Request documentation.
* Response documentation.
* Error documentation.

Documentation must remain synchronized with code.

The agent must update documentation whenever APIs change.

---

## Security Rules

All implementations must follow:

* OWASP Security Best Practices.
* JWT Security Best Practices.
* Authentication Best Practices.
* Authorization Best Practices.

Forbidden:

* Hardcoded secrets.
* Plain-text passwords.
* Exposed credentials.
* Security bypasses.
* Disabled validations.

---

## Audit Rules

All critical operations must be auditable.

Audit records must include:

* user
* action
* entity
* entity identifier
* timestamp
* IP when applicable

Audit records must be immutable.

Audit records must never be deleted or modified.

---

## Quality Rules

Every implementation must satisfy:

* 100% of defined requirements.
* 100% of applicable business rules.
* Architecture standards.
* Security standards.
* Testing standards.
* Documentation standards.

A feature is not complete until all applicable requirements have been satisfied.

---

## Definition of Done

A task is considered complete only when:

* Requirements are implemented.
* Business rules are implemented.
* Architecture standards are respected.
* Security requirements are satisfied.
* Audit requirements are satisfied.
* Tests are implemented and passing.
* Documentation is updated.
* APIs are documented.
* Domain events are implemented when required.
* No duplicated logic is introduced.
* The solution is production-ready.
