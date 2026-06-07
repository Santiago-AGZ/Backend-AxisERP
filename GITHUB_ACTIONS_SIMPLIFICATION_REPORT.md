# GITHUB ACTIONS SIMPLIFICATION REPORT

**Branch:** `fix/remove-codeql-sarif`
**Date:** 2026-06-07

---

## Changes Summary

| Metric | Value |
|--------|-------|
| Files modified | 9 |
| Lines removed | 30 |
| Lines added | 9 |
| Steps removed | 2 (`Upload Trivy filesystem results`, `Upload Docker scan results`) |
| Permissions removed | `security-events: write` (8 callers) |
| Action dependencies eliminated | `github/codeql-action/upload-sarif` |

## What Changed

### Removed (ci-template.yml)
- `github/codeql-action/upload-sarif@v3` — 2 steps removed (lines 106-111, 162-167)
- `--format sarif` + `--output *.sarif` from Trivy commands
- `-Dformat=sarif` from OWASP Dependency-Check
- Redundant `-v workspace` mount from Docker image scan

### Changed
- Trivy filesystem: `--format sarif --output file.sarif` → `--format table`
- Trivy image: `--format sarif --output file.sarif` → `--format table`

### Simplified (8 caller workflows)
- `permissions:` reduced from `contents: read, security-events: write, actions: write` → `contents: read, actions: write`

## Benefits

| Benefit | Detail |
|---------|--------|
| No CodeQL dependency | Eliminates `github/codeql-action/upload-sarif` resolution errors |
| No SARIF files | No more "Path does not exist: *.sarif" errors |
| Faster CI | 2 fewer steps, no SARIF upload overhead |
| Simpler permissions | No `security-events: write` needed |
| Console output | Trivy results visible directly in workflow logs |

## Risks

| Risk | Mitigation |
|------|------------|
| No SARIF → no GitHub Security tab | Trivy still runs with `--exit-code 1` — fails on critical/high CVEs. Output visible in console logs. |
| No CodeQL upload | CodeQL was only used for SARIF upload of Trivy results, not for actual CodeQL analysis. No loss of functionality. |

## Remaining Actions

| Action | Version | Purpose |
|--------|---------|---------|
| `actions/checkout` | `@v4.1.7` | Source checkout |
| `actions/setup-java` | `@v4.1.0` | JDK setup |
| `actions/upload-artifact` | `@v4.3.1` | Test results, JAR, SBOM |
| `docker/setup-buildx-action` | `@v3.1.0` | Docker Buildx (conditional) |
| `hadolint/hadolint-action` | `@v3.1.0` | Dockerfile lint (conditional) |
| `docker/build-push-action` | `@v6.2.0` | Docker build (conditional) |
| `aquasec/trivy` | `0.71.0` | Vulnerability scan via `docker run` |

## Commits

```
c0192e1 refactor(ci): remove codeql integration and sarif uploads
8150009 refactor(ci): remove security-events permission from all caller workflows
```

## Merge Instructions

```bash
git checkout master
git pull origin master
git merge fix/remove-codeql-sarif
git push origin master
```
