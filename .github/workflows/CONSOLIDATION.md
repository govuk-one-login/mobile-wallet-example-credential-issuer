# GitHub Actions Workflow Consolidation

Agreed approach for consolidating the repository's GitHub Actions workflows into
reusable "module" workflows pulled into **three generic top-level workflows**
(PR checks, push-to-main, dev-deploy), and the plan for applying it per service.

This ticket **applies the approach to the Document Builder service only**. Other
services (Example CRI, DSC Issuer, Platform CA, Test Harness) are left untouched
and migrated in follow-up tickets. The modules and the generic workflows are
nonetheless **designed to accommodate every service's divergence**, so follow-ups
need no module rewrites — a follow-up only adds a path filter and the service's
gated jobs to the generic workflows.

## 1. Approach

### Status (this ticket)

All seven modules are **built**. The three generic workflows are **wired for
Document Builder**:

| Module | Built | Wired by Document Builder |
|---|---|---|
| `_module-node-ci.yml` | yes | PR checks, push-to-main (incl. Node Sonar scan + gate) |
| `_module-sonarqube.yml` | yes | not wired by Document Builder; retained for the Gradle/Java path |
| `_module-checkov.yml` | yes | PR checks, push-to-main |
| `_module-docker-build-push.yml` | yes | push-to-main, dev-deploy |
| `_module-sam-deploy.yml` | yes | push-to-main, dev-deploy |
| `_module-conventional-commits.yml` | yes (stub) | no (follow-up services) |
| `_module-create-release.yml` | yes (stub) | no (follow-up services) |

> **Node coverage + Sonar must share a job.** The Sonar scan needs the Jest
> coverage report (`document-builder/coverage/lcov.info`, per
> `sonar.javascript.lcov.reportPaths`) on disk. Reusable workflows run as
> separate jobs on separate runners, so a standalone Sonar job cannot see the
> coverage produced by the Node CI job. For Node services the scan + quality
> gate therefore run **inside** `_module-node-ci.yml` (gated by `run-sonar`),
> immediately after coverage generation — matching the original per-service
> workflows. `_module-sonarqube.yml` stays for the Gradle/Java path, where
> `./gradlew sonar` generates and uploads coverage in the same step.


### Pattern: generic workflows calling reusable modules (`workflow_call`)

Each reusable job is extracted into its own callable workflow file (a "module").
**Three generic workflows** — one per trigger type — invoke the modules via
`uses:` and `with:`, forwarding secrets per the hybrid model below. This is the
pattern most aligned with the ticket requirement: "jobs are written in separate
files, and then are pulled into the main workflow when it is run."

Instead of one set of caller files **per service**, there is now **one caller
workflow per trigger** that holds every service's jobs:

- **`pull-request-checks.yml`** and **`push-to-main.yml`** begin with a
  `detect-changes` job that computes a per-service "changed" flag. Each service's
  jobs are gated on its flag (`if: needs.detect-changes.outputs.<service> == 'true'`),
  so **only changed services are built/deployed**. Both workflows end with a
  single `all-checks-passed` gate job (see §1a).
- **`dev-deploy.yml`** is a manual (`workflow_dispatch`) workflow with a
  `service` **choice** input (document-builder, and future services) plus a
  `gitRef`. Each service's deploy jobs are gated on the selected `service`, so
  exactly one service deploys per run using its own dev secrets.

```text
.github/workflows/
  _module-node-ci.yml               # reusable (workflow_call only)
  _module-sonarqube.yml             # reusable
  _module-checkov.yml               # reusable
  _module-docker-build-push.yml     # reusable
  _module-sam-deploy.yml            # reusable
  _module-conventional-commits.yml  # reusable (stub, not yet wired)
  _module-create-release.yml        # reusable (stub, not yet wired)

  pull-request-checks.yml   # generic caller: detect-changes + gated jobs + gate
  push-to-main.yml          # generic caller: detect-changes + gated jobs + gate
  dev-deploy.yml            # generic caller: service choice input + gated jobs
```

- Modules **must** live directly under `.github/workflows/`. GitHub does not
  support reusable workflows nested in subdirectories.
- The `_module-` prefix marks files that are only ever called by another
  workflow (they declare `on: workflow_call` and are never triggered directly).

## 1a. Change detection and the `all-checks-passed` gate

### Change detection

`detect-changes` runs [`dorny/paths-filter`](https://github.com/dorny/paths-filter)
(SHA-pinned to v4.0.3) and exposes a boolean-string output per service. A
service's filter matches on its own directory **and** on the workflow/module
files that affect it, so a change to a shared module re-runs the services that
use it. Example:

```yaml
document-builder:
  - 'document-builder/**'
  - '.github/workflows/pull-request-checks.yml'
  - '.github/workflows/_module-node-ci.yml'
  - '.github/workflows/_module-checkov.yml'
```

- On **pull_request**, the filter compares against the PR base automatically.
- On **push** to main, the filter compares against `github.event.before`.
- On **workflow_dispatch** of `push-to-main.yml`, the filter step is skipped and
  every service's flag is forced `true`, preserving the original per-service
  workflows' "manual run = full build/deploy" behaviour.

### The gate

Both `pull-request-checks.yml` and `push-to-main.yml` end with:

```yaml
all-checks-passed:
  name: All checks passed
  if: always()
  needs:
    - detect-changes
    - document-builder-ci
    - document-builder-checkov
  runs-on: ubuntu-24.04
  permissions:
    contents: read
  steps:
    - name: Verify all preceding jobs succeeded
      if: contains(needs.*.result, 'failure') || contains(needs.*.result, 'cancelled')
      run: exit 1
```

- `if: always()` makes the gate run even when an upstream job failed or was
  skipped.
- A **skipped** job (an unchanged service) reports `result: skipped`, which does
  **not** trip the gate — so a PR touching only one service still passes.
- The gate **fails** only on `failure` or `cancelled` among its `needs`.
- This gives branch protection **one** stable required status check
  (`All checks passed`) regardless of which services a change touches, rather
  than requiring a different per-service check for every possible change set.

### Secrets: hybrid model (least-privilege where practical)

Secrets are scoped to the jobs that actually need them, rather than inheriting
everything everywhere:

- **Jobs that need no secrets** (`_module-node-ci` → `ci`/`code-analysis`,
  `_module-checkov`) receive **no** `secrets:` block at all.
- **Jobs that need a fixed, known secret** (`_module-sonarqube`) use an
  **explicit** `secrets:` mapping forwarding only `SONAR_TOKEN`. (`GITHUB_TOKEN`
  is auto-provided to called workflows and does not need forwarding.) The
  sonarqube module declares `SONAR_TOKEN` as a required `workflow_call` secret.
- **Jobs that need dynamic, service-specific secrets** (`_module-docker-build-push`,
  `_module-sam-deploy`) use `secrets: inherit`, because the secret **name** is
  passed as an input and resolved at runtime via `secrets[inputs.<name>]` — which
  requires every candidate secret to be in scope.

This confines the broad exposure of `inherit` to only the two deploy modules,
and keeps AWS/deploy credentials out of the analysis jobs' scope.

#### secret-name-as-input (dynamic resolution)

Each service uses different secret **names** (e.g. `BUILD_DOC_BUILDER_*` vs
`BUILD_EXAMPLE_CRI_*`). To let one module serve every service, the caller passes
the **concrete secret name as a plain string input**, and the module resolves it
at runtime via the `secrets` context inside a `run:`/`env:` block:

```yaml
# caller
jobs:
  docker:
    uses: ./.github/workflows/_module-docker-build-push.yml
    with:
      role-arn-secret-name: BUILD_DOC_BUILDER_GH_ACTIONS_ROLE_ARN
    secrets: inherit

# module
env:
  ROLE_ARN: ${{ secrets[inputs.role-arn-secret-name] }}
```

**Known GitHub limitation (documented, by design):** you **cannot** index
`secrets[...]` with a dynamic key inside a reusable workflow's top-level
`secrets:` mapping, nor in a `with:` value that feeds an action expecting a
secret directly at the workflow-definition level. Dynamic resolution via
`secrets[inputs.x]` is only valid **inside a job's `env:`/`run:` context** (and
`steps.*.with` that reads from `env`). This is why the two deploy modules use
`secrets: inherit` rather than an explicit mapping — explicit mapping cannot
forward a dynamically-named secret.

### Caller input convention

Callers omit any input whose value equals the module default (the module is the
single source of truth), **except** these inputs which are always written
explicitly as readability / safety switches, even when equal to the default:

- `deploy-mode` — core behavioural switch (`upload-action` vs `s3-cp`).
- `sign` — documents a deliberate image-signing decision.
- `uses-container-image` — documents whether the SAM template is image-based.


### Core rule: add jobs, never remove

Consolidated workflows must retain **exactly** the current behaviour. Where
services diverge, we **add** optional jobs/inputs gated by conditionals — we
never drop a step or job that a service relies on today. Every module input has
a default that reproduces "do nothing extra", so wiring a service in is additive.

## 2. Module catalogue and input contracts

> Legend: `R` = required, `O` = optional (has default).

### `_module-node-ci.yml`

Node build / lint / format / test, with an optional SAM-validate step (Document
Builder runs `sam validate` inside its PR-checks job today).

| Input | Req | Default | Purpose |
|---|---|---|---|
| `working-directory` | R | — | Service dir, e.g. `./document-builder` |
| `node-version-file` | R | — | Path to `.nvmrc` |
| `install-command` | O | `npm ci` | e.g. `npm ci --ignore-scripts` |
| `run-build` | O | `false` | Run `npm run build` |
| `lint-command` | O | `""` | e.g. `npm run lint` (skipped if empty) |
| `format-check-command` | O | `""` | e.g. `npm run format:check` (skipped if empty) |
| `test-command` | O | `npm run test:cov` | Coverage command |
| `fetch-depth` | O | `0` | checkout depth |
| `run-sam-validate` | O | `false` | Run SAM validate after tests |
| `sam-version` | O | `1.90.0` | SAM CLI version when validating |
| `sam-validate-working-directory` | O | `""` | dir to `cd` into before validate |
| `sam-validate-args` | O | `--region eu-west-2` | args for `sam validate` |
| `run-sonar` | O | `false` | Run Sonar scan + gate in this job, after coverage |
| `sonar-project-base-dir` | O | `""` | e.g. `./document-builder` (required when `run-sonar`) |
| `sonar-scan-metadata-report-file` | O | `""` | report-task.txt path (required when `run-sonar`) |
| `sonar-quality-gate-timeout-minutes` | O | `5` | gate step timeout |

Secrets:

| Secret | Req | Purpose |
|---|---|---|
| `SONAR_TOKEN` | O | used by the Sonar scan + gate when `run-sonar` is true |

The scan runs immediately after the coverage step so the lcov report is on disk
in the same workspace, reproducing the original single-job behaviour.

### `_module-sonarqube.yml`

SonarQube scan + quality-gate check. **Node services do not use this module** —
their scan runs inside `_module-node-ci.yml` (see `run-sonar`) so it shares a
workspace with the Jest coverage report. This module is retained for the Gradle
(`./gradlew sonar`) path for later Java use, where coverage is produced within
the same step as the scan.

| Input | Req | Default | Purpose |
|---|---|---|---|
| `project-base-dir` | R | — | e.g. `./document-builder` |
| `scan-metadata-report-file` | R | — | report-task.txt path |
| `mode` | O | `node-scan-action` | `node-scan-action` \| `gradle` |
| `skip-for-dependabot` | O | `false` | gate steps on `github.actor != 'dependabot[bot]'` |
| `quality-gate-timeout-minutes` | O | `5` | gate step timeout |
| `java-version` | O | `17` | for `gradle` mode |
| `node-version-file` | O | `""` | checkout/node setup when needed |

Secrets (via explicit `workflow_call` `secrets:` contract):

| Secret | Req | Purpose |
|---|---|---|
| `SONAR_TOKEN` | R | used by the scan and quality-gate steps |

`GITHUB_TOKEN` is auto-provided to the called workflow and is not forwarded.
Callers forward `SONAR_TOKEN` explicitly (not via `inherit`).

### `_module-checkov.yml`

Checkov static analysis of an IaC template.

| Input | Req | Default | Purpose |
|---|---|---|---|
| `template-file` | R | — | e.g. `document-builder/deploy/template.yaml` |
| `quiet` | O | `true` | checkov `quiet` |
| `soft-fail` | O | `false` | checkov `soft_fail` |

### `_module-docker-build-push.yml`

ECR login + docker build/push, with optional Cosign signing.

| Input | Req | Default | Purpose |
|---|---|---|---|
| `working-directory` | R | — | Docker build context dir |
| `image-tag-mode` | O | `github-sha` | `github-sha` \| `git-ref-sha` |
| `registry-mode` | O | `build-account-id` | `build-account-id` \| `ecr-login-output` |
| `sign` | O | `false` | Cosign sign after push |
| `aws-region` | O | `eu-west-2` | |
| `role-arn-secret-name` | R | — | secret name for role ARN |
| `ecr-repo-secret-name` | R | — | secret name for ECR repo |
| `kms-key-secret-name` | O | `""` | secret name for Cosign KMS key (if `sign`) |
| `build-account-id-secret-name` | O | `BUILD_ACCOUNT_ID` | for `build-account-id` registry mode |
| `checkout-ref` | O | `""` | checkout a specific ref (dev-deploy uses `gitRef`) |
| `fetch-depth` | O | `0` | checkout depth; dev-deploy passes `1` for strict parity with its original shallow checkout |

Outputs: `image-tag` (resolved tag), `registry` (resolved registry).

### `_module-sam-deploy.yml`

SAM setup / validate / build / deploy, covering both deploy mechanisms.

| Input | Req | Default | Purpose |
|---|---|---|---|
| `working-directory` | R | — | SAM project dir |
| `sam-version` | O | `1.131.0` | SAM CLI version |
| `deploy-mode` | O | `upload-action` | `upload-action` \| `s3-cp` |
| `uses-container-image` | O | `false` | do the `CONTAINER-IMAGE-PLACEHOLDER` sed |
| `sam-build-args` | O | `""` | e.g. `--cached` |
| `sam-validate-args` | O | `""` | e.g. `--lint` |
| `signing-profile-secret-name` | O | `""` | upload-action `signing-profile-name` (empty = not passed) |
| `bucket-name-secret-name` | R | — | artifact bucket secret name |
| `upload-working-directory` | O | `""` | upload-action `working-directory` (empty = `working-directory`) |
| `template-file` | O | `""` | upload-action `template-file` (empty = not passed) |
| `role-arn-secret-name` | O | `""` | AWS creds (set to configure creds in any mode) |
| `ecr-repo-secret-name` | O | `""` | image repo for sed (if container image) |
| `registry` | O | `""` | registry for sed (if container image) |
| `image-tag` | O | `""` | tag for sed (if container image) |
| `checkout-ref` | O | `""` | checkout a specific ref |

- `upload-action` mode → `govuk-one-login/devplatform-upload-action`. The deploy
  step exists in gated variants so `template-file` / `signing-profile-name` are
  **absent** (not empty-string) when a service does not use them, preserving
  exact per-service parity (Document Builder passes neither).
- `s3-cp` mode → `sam build`, `mv .aws-sam/build/template.yaml cf-template.yaml`,
  sed placeholder, `zip template.zip cf-template.yaml`, `aws s3 cp ... --metadata`.

### `_module-conventional-commits.yml` (stub — not wired this ticket)

cocogitto `check --from-latest-tag` on the PR head. For Example CRI / DSC Issuer.

### `_module-create-release.yml` (stub — not wired this ticket)

cocogitto `bump --auto` + GitHub release. For Example CRI / DSC Issuer.

## 3. Per-service divergence matrix

Captured from the current workflow files so modules cover every case. Document
Builder (**DB**) is the only service wired this ticket.

### Pull-request checks

| Aspect | DB | Example CRI | DSC Issuer | Platform CA | Test Harness |
|---|---|---|---|---|---|
| Concurrency group | no | yes | yes | yes | (separate) |
| Conventional-commits job | no | yes | yes | no | — |
| Integration-tests job | no | yes | no | no | — |
| Toolchain | Node | Java (Gradle/JDK17) | Node | Node (`utils/`) | Node |
| `npm run build` | yes | — (gradle build) | no | no | — |
| `npm run lint` | yes | — | no | yes | — |
| `npm run format:check` | no | — | yes | yes | — |
| SonarQube scan + gate | yes (scan-action) | yes (gradle sonar) | yes (scan-action) | no | (separate) |
| SAM validate | yes (`--region eu-west-2`) | no | no | yes (`--lint`) | — |
| SAM build in PR | no | no | no | yes (`--parallel`) | — |
| Checkov | yes | yes | yes | yes | — |
| SAM version | 1.90.0 | — | — | 1.160.0 | — |

### Push to main

| Aspect | DB | Example CRI | DSC Issuer | Platform CA |
|---|---|---|---|---|
| Code-analysis job | inline (one job) | separate jobs | one job | none |
| Sonar split into own job | no | yes | no | no |
| Integration-tests job | no | yes | no | no |
| Docker build/push | yes | yes | no | no |
| Cosign sign | yes | yes | no | no |
| Image tag | `$GITHUB_SHA` | `$GITHUB_SHA` | — | — |
| Registry | `BUILD_ACCOUNT_ID` literal | `BUILD_ACCOUNT_ID` literal | — | — |
| SAM deploy mechanism | upload-action | upload-action | upload-action | upload-action |
| `signing-profile-name` | no | no | no | yes (`BUILD_SIGNING_PROFILE_NAME`) |
| SAM version | 1.90.0 | 1.131.0 | 1.131.0 | 1.160.0 |
| SAM build args | `--cached` | (none) | (none) | `--parallel` |
| SAM validate args | (none) | (none) | (none) | `--lint` |
| create-release job | no | yes | yes | no |
| Role-ARN secret | `BUILD_DOC_BUILDER_GH_ACTIONS_ROLE_ARN` | `BUILD_EXAMPLE_CRI_GH_ACTIONS_ROLE_ARN` | `BUILD_DSC_ISS_GH_ACTIONS_ROLE_ARN` | `BUILD_PLATFORM_CA_GH_ACTIONS_ROLE_ARN` |

### Deploy to dev (`workflow_dispatch`, `gitRef` input)

| Aspect | DB | Example CRI | DSC Issuer | Platform CA |
|---|---|---|---|---|
| Docker build/push | yes | yes | no | no |
| Image tag | `GIT_REF_SHA` | `GIT_REF_SHA` | — | — |
| Registry | ECR-login output | ECR-login output | — | — |
| SAM deploy mechanism | `s3-cp` (zip + `aws s3 cp`) | `s3-cp` | upload-action | upload-action |
| `signing-profile-name` | no | no | yes (`DEV_SIGNING_PROFILE_NAME`) | yes (`DEV_SIGNING_PROFILE_NAME`) |
| SAM version | 1.131.0 | 1.131.0 | 1.131.0 | 1.160.0 |
| Role-ARN secret | `DEV_DOC_BUILDER_GH_ACTIONS_ROLE_ARN` | `DEV_EXAMPLE_CRI_*` | `DEV_DSC_ISS_GH_ACTIONS_ROLE_ARN` | `DEV_PLATFORM_CA_GH_ACTIONS_ROLE_ARN` |

### Shared secrets

`BUILD_ACCOUNT_ID`, `SONAR_TOKEN`, `GITHUB_TOKEN` (auto), `BUILD_SIGNING_PROFILE_NAME`,
`DEV_SIGNING_PROFILE_NAME`.

## 4. Document Builder wiring (this ticket)

Document Builder's jobs live inside the three **generic** workflows, gated by
change detection (PR checks, push-to-main) or the `service` choice (dev-deploy).
They wire exactly DB's current jobs — no conventional-commits, no create-release,
no integration-tests:

- **`pull-request-checks.yml`** → `detect-changes` →
  `document-builder-ci` (`_module-node-ci`: build+lint+test+`sam validate`),
  `document-builder-sonarqube` (`_module-sonarqube`),
  `document-builder-checkov` (`_module-checkov`) →
  `all-checks-passed` gate. Each DB job is gated on
  `needs.detect-changes.outputs.document-builder == 'true'`.
- **`push-to-main.yml`** → `detect-changes` →
  `document-builder-code-analysis` (`_module-node-ci`, test only),
  `document-builder-checkov`, `document-builder-sonarqube` →
  `document-builder-docker` (`_module-docker-build-push`: sign, `github-sha`,
  `build-account-id`) → `document-builder-sam-deploy` (`_module-sam-deploy`:
  `upload-action`, SAM 1.90.0, `--cached`, container image) →
  `all-checks-passed` gate. Gated on the DB change flag (forced `true` on manual
  `workflow_dispatch`).
- **`dev-deploy.yml`** → `document-builder-docker` (`_module-docker-build-push`:
  `git-ref-sha`, `ecr-login-output`, no sign) → `document-builder-sam-deploy`
  (`_module-sam-deploy`: `s3-cp`, SAM 1.131.0, container image). Gated on
  `inputs.service == 'document-builder'`.

## 5. Verification

- Agent verification only: `actionlint`, YAML syntax, and line-by-line parity
  diffs against git history. **The agent does not trigger live runs.**
- Live/functional testing (PR checks, push-to-main, dev deploy via
  `workflow_dispatch`) is performed by the maintainers — see the migration
  checklist below. Maintainers should set **`All checks passed`** as the single
  required status check for `pull-request-checks.yml` in branch protection.
- `actionlint` baseline note: the reusable modules already emit informational
  `shellcheck` quoting notes (SC2086/SC2006). These are pre-existing and are
  preserved for behaviour parity; the refactor introduces **no new error-level
  findings**.

## 6. Migration checklist (follow-up services)

For each of Example CRI, DSC Issuer, Platform CA, Test Harness, extend the three
**generic** workflows (do not re-introduce per-service caller files):

1. **Change detection:** add a `<service>` filter to the `detect-changes` job in
   `pull-request-checks.yml` and `push-to-main.yml` (its own `**` path plus the
   module files it uses), and add a matching `<service>` output.
2. **Jobs:** add the service's gated jobs (`if: needs.detect-changes.outputs.<service> == 'true'`),
   wiring the jobs the service has today (add the conventional-commits /
   create-release / integration-tests / Sonar-split jobs where present).
3. **Gate:** add every new job to the `all-checks-passed` gate's `needs` list.
4. **Secrets:** map the service's concrete secret names to the module
   secret-name inputs. Apply the hybrid secrets model: forward `SONAR_TOKEN`
   **explicitly** on the sonarqube caller job; use `secrets: inherit` only on the
   `docker` / `sam-deploy` caller jobs (which resolve secrets dynamically); pass
   no `secrets:` block on node-ci / checkov jobs.
5. **Divergent inputs:** set SAM version, deploy mode, build/validate args,
   `signing-profile-secret-name`, toolchain (`mode: gradle` for Example CRI).
6. **Dev deploy:** add the service to the `service` choice `options` in
   `dev-deploy.yml` and add its gated deploy jobs
   (`if: inputs.service == '<service>'`).
7. Run `actionlint` and diff behaviour against the pre-migration file.
8. Maintainer runs the live checks (PR, push-to-main, dev deploy).
