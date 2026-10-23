# GitHub Actions Workflow Consolidation

Agreed approach for consolidating the repository's GitHub Actions workflows into
reusable "module" workflows, and the plan for applying it per service.

This ticket **applies the approach to the Document Builder service only**. Other
services (Example CRI, DSC Issuer, Platform CA, Test Harness) are left untouched
and migrated in follow-up tickets. The modules are nonetheless **designed to
accommodate every service's divergence**, so follow-ups need no module rewrites.

## 1. Approach

### Status (this ticket)

All seven modules are **built**. Document Builder's three callers are **wired**:

| Module | Built | Wired by Document Builder |
|---|---|---|
| `_module-node-ci.yml` | yes | PR checks, push-to-main |
| `_module-sonarqube.yml` | yes | PR checks, push-to-main |
| `_module-checkov.yml` | yes | PR checks, push-to-main |
| `_module-docker-build-push.yml` | yes | push-to-main, dev-deploy |
| `_module-sam-deploy.yml` | yes | push-to-main, dev-deploy |
| `_module-conventional-commits.yml` | yes (stub) | no (follow-up services) |
| `_module-create-release.yml` | yes (stub) | no (follow-up services) |


### Pattern: reusable workflows (`workflow_call`)

Each reusable job is extracted into its own callable workflow file (a "module").
Thin per-service **caller** workflows invoke the modules via `uses:` and `with:`,
forwarding secrets per the hybrid model below. This is the pattern most aligned
with the ticket requirement: "jobs are written in separate files, and then are
pulled into the main workflow when it is run."

```text
.github/workflows/
  _module-node-ci.yml               # reusable (workflow_call only)
  _module-sonarqube.yml             # reusable
  _module-checkov.yml               # reusable
  _module-docker-build-push.yml     # reusable
  _module-sam-deploy.yml            # reusable
  _module-conventional-commits.yml  # reusable (stub, not yet wired)
  _module-create-release.yml        # reusable (stub, not yet wired)

  document-builder-pull-request-checks.yml   # caller
  document-builder-push-to-main.yml          # caller
  document-builder-dev-deploy.yml            # caller
```

- Modules **must** live directly under `.github/workflows/`. GitHub does not
  support reusable workflows nested in subdirectories.
- The `_module-` prefix marks files that are only ever called by another
  workflow (they declare `on: workflow_call` and are never triggered directly).

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

### `_module-sonarqube.yml`

SonarQube scan + quality-gate check. Supports the Node scan-action path now and
a Gradle (`./gradlew sonar`) path gated by input for later Java use.

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

The three DB callers wire exactly DB's current jobs — no conventional-commits,
no create-release, no integration-tests:

- **PR checks** → `_module-node-ci` (build+lint+test+`sam validate`) →
  `_module-sonarqube` → `_module-checkov`.
- **Push to main** → `_module-node-ci` (test only) + `_module-checkov` +
  `_module-sonarqube` → `_module-docker-build-push` (sign, `github-sha`,
  `build-account-id`) → `_module-sam-deploy` (`upload-action`, SAM 1.90.0,
  `--cached`, container image).
- **Deploy to dev** → `_module-docker-build-push` (`git-ref-sha`,
  `ecr-login-output`, no sign) → `_module-sam-deploy` (`s3-cp`, SAM 1.131.0,
  container image).

## 5. Verification

- Agent verification only: `actionlint`, YAML syntax, and line-by-line parity
  diffs against git history. **The agent does not trigger live runs.**
- Live/functional testing (PR checks, push-to-main, dev deploy via
  `workflow_dispatch`) is performed by the maintainers — see the migration
  checklist below.
- `actionlint` baseline note: the pre-existing workflows already emit
  informational `shellcheck` quoting notes (SC2086/SC2006) and `inputs.gitRef`
  expression warnings on push-to-main files. These are pre-existing and are
  preserved for behaviour parity; the refactor introduces **no new error-level
  findings**.

## 6. Migration checklist (follow-up services)

For each of Example CRI, DSC Issuer, Platform CA, Test Harness:

1. Create thin caller workflows preserving the existing `name`, `on`
   triggers/`paths`, `permissions`, and `concurrency` blocks.
2. Map the service's concrete secret names to the module secret-name inputs.
   Apply the hybrid secrets model: forward `SONAR_TOKEN` **explicitly** on the
   sonarqube caller job; use `secrets: inherit` only on the `docker` /
   `sam-deploy` caller jobs (which resolve secrets dynamically); pass no
   `secrets:` block on node-ci / checkov jobs.
3. Wire the jobs the service has today (add the conventional-commits /
   create-release / integration-tests / Sonar-split jobs where present).
4. Set divergent inputs: SAM version, deploy mode, build/validate args,
   `signing-profile-secret-name`, toolchain (`mode: gradle` for Example CRI).
5. Run `actionlint` and diff behaviour against the pre-migration file.
6. Maintainer runs the live checks (PR, push-to-main, dev deploy).
