# ARCHITECTURE.md

# AxisERP Architecture

## Architecture Style

AxisERP follows:

* Domain Driven Design (DDD)
* Hexagonal Architecture
* Clean Architecture
* Microservices Architecture

The architecture must ensure:

* High cohesion.
* Low coupling.
* Independent deployment.
* Independent scalability.
* Domain isolation.
* Clear ownership of business capabilities.

---

## Technology Stack

### Backend

* Java 21
* Spring Boot 3.5.x
* Maven

### Database

* PostgreSQL
* Neon PostgreSQL

### Messaging

* RabbitMQ

### Security

* JWT ES256
* Supabase JWKS
* OWASP Security Best Practices

### Infrastructure

* Docker
* Docker Compose

---

## Microservices

AxisERP is composed of the following services:

* api-gateway
* auth-service
* catalog-service
* inventory-service
* purchase-service
* sales-service
* report-service

Each service is independently deployable and independently maintainable.

---

## Domain Ownership

Each microservice owns its domain.

Rules:

* A service may only modify its own data.
* A service may only directly access its own database.
* A service may expose APIs to other services.
* A service may publish domain events.
* A service may consume domain events.
* A service must not modify another service's database.
* A service must not bypass another service's public APIs.

---

## Data Architecture

AxisERP follows the Database-per-Service pattern.

Each microservice owns a dedicated database.

Examples:

* auth-db
* catalog-db
* inventory-db
* purchase-db
* sales-db
* report-db

Rules:

* No shared database.
* No direct database access between services.
* No cross-service joins.
* No foreign keys between databases.
* No shared schemas.
* No shared tables.
* Database ownership belongs exclusively to the corresponding service.

---

## Communication

AxisERP uses both synchronous and asynchronous communication.

### Synchronous Communication

REST APIs

Used for:

* Queries
* Validation requests
* Immediate operations
* User-driven actions

Rules:

* Services communicate through public APIs only.
* APIs must be documented using OpenAPI.
* APIs must follow REST standards.
* APIs must expose standardized responses.

### Asynchronous Communication

RabbitMQ Domain Events

Used for:

* Event propagation
* Cross-service notifications
* Eventual consistency
* Decoupled workflows

---

## Messaging Architecture

RabbitMQ is used for Domain Events.

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

Rules:

* Domain events must represent completed business actions.
* Events must be immutable.
* Events must contain sufficient context.
* Services must remain loosely coupled.
* Services must not depend on event processing order unless explicitly defined.

---

## Consistency Strategy

AxisERP uses Eventual Consistency between microservices.

Rules:

* Cross-service operations must use domain events.
* Services must not depend on distributed transactions.
* Services must remain independently deployable.
* Services must remain independently scalable.
* Services must tolerate temporary inconsistencies.
* Consistency must be achieved through event propagation.

---

## API Documentation

All services must provide:

* OpenAPI documentation.
* Swagger UI in development environments.
* Automatic API documentation generation.

Requirements:

* All endpoints documented.
* All request DTOs documented.
* All response DTOs documented.
* All error responses documented.
* Documentation must remain synchronized with implementation.

---

## Security Architecture

### Authentication

Authentication is based on:

* JWT ES256
* Supabase JWKS

Requirements:

* JWT signature validation.
* JWT expiration validation.
* JWT integrity validation.
* JWT claims validation.

### Authorization

Authorization follows Role-Based Access Control (RBAC).

Roles:

* ADMIN
* VENDEDOR
* INVENTARIO

Requirements:

* Authorization checks required before protected operations.
* Unauthorized access must be denied.
* Role permissions must be enforced consistently.

### Security Standards

All services must follow:

* OWASP recommendations.
* Secure password storage.
* Secure token handling.
* Input validation.
* Output sanitization where applicable.
* Audit logging of critical actions.

---

## Audit Architecture

Each microservice manages its own audit trail.

Audit records must be immutable.

Audit records must include:

* User
* Action
* Date
* Entity
* Entity Identifier
* IP when applicable

Rules:

* Audit records cannot be modified.
* Audit records cannot be deleted.
* Critical business operations must be audited.
* Security-related operations must be audited.

---

## Business Rules

All business rules are defined in:

* BUSINESS_RULES.md

Rules:

* Business rules are mandatory.
* Business rules have higher priority than implementation convenience.
* Architecture decisions must never violate business rules.
* Implementations must respect all business rules.

---

## Development Principles

Every implementation must respect:

* DDD principles.
* Hexagonal Architecture principles.
* SOLID principles.
* Clean Code principles.
* Separation of concerns.
* Single Responsibility Principle.
* Domain ownership boundaries.

The architecture must prioritize:

* Maintainability.
* Scalability.
* Testability.
* Security.
* Reliability.
* Long-term evolution.

---

## Future Evolution

Potential future services may include:

* audit-service
* notification-service

Current architecture must not prevent future extraction of these capabilities into independent services.
