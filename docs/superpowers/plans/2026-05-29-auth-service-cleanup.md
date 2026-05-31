# Auth Service Dead Code Cleanup Implementation Plan

> **Goal:** Remove all dead/orphaned code and DB objects from auth-service, fix FirstLoginFilter to use factory pattern properly.

**Architecture:** Hexagonal. Changes span domain model, JPA entity, adapter, factory, config, pom.xml, application.properties, tests, and Neon database.

---
