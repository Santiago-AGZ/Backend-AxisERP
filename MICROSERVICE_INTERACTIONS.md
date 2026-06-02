# MICROSERVICE_INTERACTIONS.md

# AxisERP Microservice Interactions

## Purpose

This document defines how microservices communicate with each other.

Rules defined here are mandatory.

Services must respect domain ownership and service boundaries.

---

# Communication Principles

Allowed communication:

## Synchronous

* REST APIs

## Asynchronous

* RabbitMQ Domain Events

Forbidden:

* Direct database access between services.
* Shared databases.
* Shared tables.
* Cross-service joins.
* Distributed transactions.

---

# Service Responsibilities

## auth-service

Owns:

* Users
* Roles
* Authentication
* Authorization
* Password Management
* Sessions

Publishes:

* UsuarioCreado
* UsuarioActualizado
* UsuarioEliminado
* PasswordCambiada

Consumes:

* None

---

## catalog-service

Owns:

* Categories
* Products
* Product Codes
* Product Barcodes

Publishes:

* CategoriaCreada
* CategoriaActualizada
* ProductoCreado
* ProductoActualizado
* ProductoEliminado

Consumes:

* None

---

## inventory-service

Owns:

* Inventory
* Stock
* Inventory Movements
* Inventory Alerts

Publishes:

* StockActualizado
* StockBajo
* StockAgotado

Consumes:

* CompraRecibida
* CompraAnulada
* VentaConfirmada
* VentaAnulada

---

## purchase-service

Owns:

* Suppliers
* Purchases

Publishes:

* CompraCreada
* CompraRecibida
* CompraAnulada

Consumes:

* ProductoCreado
* ProductoActualizado

---

## sales-service

Owns:

* Customers
* Sales
* Invoices

Publishes:

* VentaCreada
* VentaConfirmada
* VentaAnulada
* FacturaGenerada

Consumes:

* StockActualizado

---

## report-service

Owns:

* Reports
* Dashboards
* Exports

Publishes:

* ReporteGenerado

Consumes:

* ProductoCreado
* ProductoActualizado
* CompraRecibida
* CompraAnulada
* VentaConfirmada
* VentaAnulada
* FacturaGenerada
* StockActualizado
* StockBajo
* StockAgotado

---

## api-gateway

Responsibilities:

* Routing
* Authentication forwarding
* Rate limiting
* Request correlation

Business logic is forbidden.

---

# REST Interactions

Allowed examples:

## sales-service → inventory-service

Purpose:

* Stock validation before confirming sale.

---

## purchase-service → catalog-service

Purpose:

* Product validation.

---

## sales-service → catalog-service

Purpose:

* Product validation.

---

## Any Service → auth-service

Purpose:

* Authentication and authorization support.

---

# RabbitMQ Interactions

## Purchase Flow

purchase-service

Publishes:

* CompraRecibida

inventory-service

Consumes:

* CompraRecibida

Effect:

* Increase stock.

---

## Sale Flow

sales-service

Publishes:

* VentaConfirmada

inventory-service

Consumes:

* VentaConfirmada

Effect:

* Decrease stock.

---

## Sale Cancellation Flow

sales-service

Publishes:

* VentaAnulada

inventory-service

Consumes:

* VentaAnulada

Effect:

* Restore stock.

---

## Purchase Cancellation Flow

purchase-service

Publishes:

* CompraAnulada

inventory-service

Consumes:

* CompraAnulada

Effect:

* Reverse inventory movements.

---

## Invoice Flow

sales-service

Publishes:

* FacturaGenerada

report-service

Consumes:

* FacturaGenerada

Effect:

* Reporting and analytics update.

---

# Database Rules

Each service owns its database.

Examples:

* auth-db
* catalog-db
* inventory-db
* purchase-db
* sales-db
* report-db

Rules:

* No shared database.
* No direct database access.
* No cross-service joins.
* No foreign keys across services.

---

# Consistency Rules

AxisERP uses Eventual Consistency.

Rules:

* Cross-service workflows must use domain events.
* Services must tolerate temporary inconsistencies.
* Services must remain independently deployable.
* Services must remain independently scalable.

---

# Future Evolution

Potential future services:

* notification-service
* audit-service

Current implementations must not prevent future extraction into dedicated services.
