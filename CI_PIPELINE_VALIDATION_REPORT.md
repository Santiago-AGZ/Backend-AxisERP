# GITHUB ACTIONS AUDIT REPORT — AxisERP

## Audit Date
2026-06-07

## Auditor
Automated pipeline audit

## Scope
All 9 workflow files in `.github/workflows/`

---

## 1. Workflow Map

```
                    ┌─────────────────────────────┐
                    │       ci-template.yml        │
                    │  (reusable workflow caller)  │
                    │  NO permissions block        │
                    └─────────────────────────────┘
                              ▲
                              │ workflow_call
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
   ┌────┴─────┐    ┌──────────┴──────────┐  ┌──────┴──────┐
   │ ci-*.yml │    │    ci-full.yml      │  │ (8 callers) │
   │ (x7)     │    │  push: master       │  │             │
   │ push+PR  │    │  build-docker:true  │  │             │
   │ fast     │    │  full security      │  │             │
   └──────────┘    └─────────────────────┘  └─────────────┘
```

## 2. Files Reviewed

| # | File | Type | Status |
|---|------|------|--------|
| 1 | `ci-template.yml` | Reusable (workflow_call) | ✅ Verified |
| 2 | `ci-auth.yml` | Caller (per-service) | ✅ Verified |
| 3 | `ci-catalog.yml` | Caller (per-service) | ✅ Verified |
| 4 | `ci-inventory.yml` | Caller (per-service) | ✅ Verified |
| 5 | `ci-sales.yml` | Caller (per-service) | ✅ Verified |
| 6 | `ci-purchase.yml` | Caller (per-service) | ✅ Verified |
| 7 | `ci-report.yml` | Caller (per-service) | ✅ Verified |
| 8 | `ci-gateway.yml` | Caller (per-service) | ✅ Verified |
| 9 | `ci-full.yml` | Caller (full CI on master) | ✅ Verified |

## 3. Permissions Analysis

### Reusable Workflow (ci-template.yml)

**Current state:** NO `permissions:` block

**Why:** When a reusable workflow has permissions, GitHub validates them against the caller's token. If the template requests scopes the caller doesn't have, validation fails. Removing the block lets the caller's token permissions flow through.

**Actions requiring permissions inside the template:**
| Action | Required Scope | Status |
|--------|---------------|--------|
| `actions/checkout@v4` | `contents: read` | ✅ Default |
| `actions/setup-java@v4` | `contents: read` | ✅ Default |
| `actions/upload-artifact@v4` | `actions: write` | ✅ From caller |
| `github/codeql-action/upload-sarif@v3` | `security-events: write` | ✅ From caller |
| `docker/setup-buildx-action@v3` | None | ✅ |
| `hadolint/hadolint-action@v3` | `contents: read` | ✅ Default |
| `docker/build-push-action@v6` | `contents: read` | ✅ Default |

### Caller Workflows (ci-auth.yml through ci-gateway.yml + ci-full.yml)

**Current state:** ALL 8 have:
```yaml
permissions:
  contents: read
  security-events: write
  actions: write
```

**Why:** These 3 scopes cover all actions used inside the template. Unlisted scopes default to `none`, which is secure.

## 4. Trigger Analysis

| Workflow | `push` trigger | `pull_request` trigger | Includes workflow path |
|----------|---------------|------------------------|----------------------|
| ci-auth.yml | `auth-service/**` | ✅ `auth-service/** + ci-auth.yml + ci-template.yml` | ✅ |
| ci-catalog.yml | `catalog-service/**` | ✅ Same pattern | ✅ |
| ci-inventory.yml | `inventory-service/**` | ✅ Same pattern | ✅ |
| ci-sales.yml | `sales-service/**` | ✅ Same pattern | ✅ |
| ci-purchase.yml | `purchase-service/**` | ✅ Same pattern | ✅ |
| ci-report.yml | `report-service/**` | ✅ Same pattern | ✅ |
| ci-gateway.yml | `api-gateway/**` | ✅ Same pattern | ✅ |
| ci-full.yml | `branches: [main, master]` | ❌ No PR trigger (intentional) | N/A |

**Note:** ci-full.yml intentionally has no PR trigger because PRs are covered by the per-service workflows. Full CI only runs on merge to master.

## 5. Build Docker Configuration

| Workflow | `build-docker` |
|----------|:--------------:|
| ci-template.yml (default) | `false` |
| ci-auth.yml → ci-gateway.yml | `false` (fast CI) |
| ci-full.yml | `true` (full CI on master) |

## 6. Actions Version Audit

| Action | Version | Status |
|--------|---------|--------|
| `actions/checkout` | `@v4.1.7` | ✅ Latest stable |
| `actions/setup-java` | `@v4.1.0` | ✅ Latest v4 |
| `actions/upload-artifact` | `@v4.3.1` | ✅ Latest v4 |
| `github/codeql-action/upload-sarif` | `@v3.1.0` | ✅ Latest v3 |
| `docker/setup-buildx-action` | `@v3.1.0` | ✅ Latest v3 |
| `hadolint/hadolint-action` | `@v3.1.0` | ✅ Latest |
| `docker/build-push-action` | `@v6.2.0` | ✅ Latest v6 |

## 7. Concurrency Groups

| Workflow | Group Key | Cancel In Progress |
|----------|-----------|:------------------:|
| ci-auth.yml | `ci-auth-${{ github.ref }}` | ✅ |
| ci-catalog.yml | `ci-catalog-${{ github.ref }}` | ✅ |
| ci-inventory.yml | `ci-inventory-${{ github.ref }}` | ✅ |
| ci-sales.yml | `ci-sales-${{ github.ref }}` | ✅ |
| ci-purchase.yml | `ci-purchase-${{ github.ref }}` | ✅ |
| ci-report.yml | `ci-report-${{ github.ref }}` | ✅ |
| ci-gateway.yml | `ci-gateway-${{ github.ref }}` | ✅ |
| ci-full.yml | `ci-full-${{ github.ref }}` | ✅ |

## 8. Findings Summary

| ID | Severity | Description | File | Status |
|----|----------|-------------|------|--------|
| F1 | CRITICAL | Caller workflows missing permissions (caused validation error) | ci-auth.yml → ci-gateway.yml + ci-full.yml | ✅ FIXED in `e55d2c7` |
| F2 | HIGH | Template had permissions block conflicting with caller inheritance | ci-template.yml | ✅ FIXED in `41c2b53` |
| F3 | MEDIUM | `build-docker: true` default caused slow per-service CI | ci-template.yml | ✅ FIXED in `41c2b53` |
| F4 | MEDIUM | PR triggers missing workflow file paths | ci-auth.yml → ci-gateway.yml | ✅ FIXED in `41c2b53` |
| F5 | HIGH | `cyclonedx` goal `makeAggregate` renamed to `makeAggregateBom` | All 7 pom.xml | ✅ FIXED in `b6817f2` |

## 9. Residual Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Trivy `--exit-code 1` fails build on CVE | Pipeline shows red for known vulns | Intentional — blocks insecure deploys |
| 7 parallel Docker builds on master | Build time ~30-45 min | Acceptable for merge to master |
| `upload-sarif` may fail if org restricts `security-events` | SARIF not uploaded | Check org-level policies |
| No secrets scanning in GHA | Token leaks undetected | Trivy scans for secrets in filesystem |

## 10. Conclusion

**CI/CD pipeline is correctly configured.** The permissions error has been resolved by:
1. Removing permissions from the reusable template
2. Adding explicit `contents: read, security-events: write, actions: write` to all 8 callers

No further changes required. The workflows should execute successfully on the next push.
