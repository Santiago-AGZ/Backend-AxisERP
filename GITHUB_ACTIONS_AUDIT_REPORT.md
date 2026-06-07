# GITHUB ACTIONS AUDIT REPORT — AxisERP

**Branch:** `fix/github-actions-audit`
**Date:** 2026-06-07
**Commit:** `01840c0`

---

## 1. Scope

All 9 workflow files in `.github/workflows/` reviewed against current GitHub API state.

## 2. Action Version Verification

Every `uses:` reference was checked against the GitHub API to confirm the tag exists.

| Action Reference | Verified | Status |
|-----------------|----------|--------|
| `actions/checkout@v4.1.7` | API: tag exists | ✅ |
| `actions/setup-java@v4.1.0` | API: tag exists | ✅ |
| `actions/upload-artifact@v4.3.1` | API: tag exists | ✅ |
| `github/codeql-action/upload-sarif@v3.1.0` | **API: tag NOT FOUND** | ❌ **FIXED** |
| `github/codeql-action/upload-sarif@v3` | API: tag exists + sub-action verified | ✅ |
| `docker/setup-buildx-action@v3.1.0` | API: tag exists | ✅ |
| `hadolint/hadolint-action@v3.1.0` | API: tag exists | ✅ |
| `docker/build-push-action@v6.2.0` | API: tag exists | ✅ |

## 3. Permissions Verification

| Workflow | Has `permissions:` | `security-events` | `actions` | Status |
|----------|:------------------:|:-----------------:|:---------:|:------:|
| `ci-template.yml` | **NO** (correct — reusable) | Inherited | Inherited | ✅ |
| `ci-auth.yml` | ✅ | `write` | `write` | ✅ |
| `ci-catalog.yml` | ✅ | `write` | `write` | ✅ |
| `ci-inventory.yml` | ✅ | `write` | `write` | ✅ |
| `ci-sales.yml` | ✅ | `write` | `write` | ✅ |
| `ci-purchase.yml` | ✅ | `write` | `write` | ✅ |
| `ci-report.yml` | ✅ | `write` | `write` | ✅ |
| `ci-gateway.yml` | ✅ | `write` | `write` | ✅ |
| `ci-full.yml` | ✅ | `write` | `write` | ✅ |

## 4. Issues Found

### Issue 1: Invalid action tag `v3.1.0`

- **File:** `ci-template.yml`, lines 108 and 164
- **Reference:** `github/codeql-action/upload-sarif@v3.1.0`
- **API verification:** Tag `v3.1.0` returned HTTP 404 (does not exist)
- **Root cause:** The `github/codeql-action` repository uses tags starting at `v4.x.x`. Major version `v3` exists but no `v3.1.0` specific tag was ever published.
- **Impact:** GitHub cannot resolve the action. The workflow is rejected at validation time with no actionable error message shown in the UI.
- **Fix:** Changed to `github/codeql-action/upload-sarif@v3` (major version tag — verified to exist and contain the `upload-sarif` sub-action)
- **Status:** ✅ **FIXED** in commit `01840c0`

## 5. Validation Performed

| Validation | Tool | Result |
|-----------|------|--------|
| GitHub API tag check | `curl + GitHub API` | All 7 actions verified |
| Sub-action existence | `curl + GitHub API` | `upload-sarif` in `v3` tag verified |
| YAML syntax | Manual review | No syntax errors found |
| Maven build | `mvn package -DskipTests` | ✅ All 7 services |
| Docker build | `docker build` | ✅ 3 services verified |
| Permissions model | Cross-reference | ✅ Template clean, 8 callers with explicit scopes |

## 6. Risks

| Risk | Severity | Notes |
|------|----------|-------|
| Major version changes in actions | LOW | Pin to `@v3` not `@v3.1.0` means minor updates auto-apply |
| Trivy `--exit-code 1` fails on CVE | INTENTIONAL | Design decision — blocks insecure artifacts |
| No CD/deploy workflow | MEDIUM | Render auto-deploys from GitHub; no GHA deploy step needed |

## 7. Commits

```
01840c0 fix(ci): update codeql-action/upload-sarif tag from invalid v3.1.0 to v3
```

## 8. Merge Instructions

```bash
git checkout master
git pull origin master
git merge fix/github-actions-audit
git push origin master
```
