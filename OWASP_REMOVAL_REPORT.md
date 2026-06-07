# OWASP Dependency Check Removal Report

**Branch:** `fix/remove-owasp-dependency-check`
**Date:** 2026-06-07

---

## Problem

OWASP Dependency Check was failing with:
```
UpdateException: Error updating the NVD Data
NVD returned 403/404
NoDataException: No documents exist
```

This required an `NVD_API_KEY` (free but rate-limited) and made CI dependent on an external service that was returning errors.

## Changes

### Files Modified: 8

| File | Change | Lines Removed |
|------|--------|:-------------:|
| `.github/workflows/ci-template.yml` | Removed `OWASP Dependency-Check scan` step | 8 |
| `auth-service/pom.xml` | Removed `dependency-check-maven` plugin | 18 |
| `catalog-service/pom.xml` | Removed `dependency-check-maven` plugin | 18 |
| `inventory-service/pom.xml` | Removed `dependency-check-maven` plugin | 18 |
| `sales-service/pom.xml` | Removed `dependency-check-maven` plugin | 18 |
| `purchase-service/pom.xml` | Removed `dependency-check-maven` plugin | 18 |
| `report-service/pom.xml` | Removed `dependency-check-maven` plugin | 18 |
| `api-gateway/pom.xml` | Removed `dependency-check-maven` plugin | 18 |
| **Total** | | **134** |

## Validation

| Service | `mvn clean verify` | Status |
|---------|:------------------:|:------:|
| auth-service | BUILD SUCCESS | ✅ |
| catalog-service | BUILD SUCCESS | ✅ |
| inventory-service | BUILD SUCCESS | ✅ |
| sales-service | BUILD SUCCESS | ✅ |
| purchase-service | BUILD SUCCESS | ✅ |
| report-service | BUILD SUCCESS | ✅ |
| api-gateway | BUILD SUCCESS | ✅ |

## Security Coverage (post-removal)

| Tool | Purpose | Status |
|------|---------|--------|
| Trivy (filesystem) | Dependency + secret scanning | ✅ Still active |
| Trivy (Docker) | Container image scanning | ✅ Still active |
| CycloneDX SBOM | Software Bill of Materials | ✅ Still active (via `makeAggregateBom`) |

## Commits

```
600740e refactor(ci): remove dependency-check workflow step
80ddc73 refactor(build): remove dependency-check plugin from all pom.xml
```

## Merge Instructions

```bash
git checkout master
git pull origin master
git merge fix/remove-owasp-dependency-check
git push origin master
```
