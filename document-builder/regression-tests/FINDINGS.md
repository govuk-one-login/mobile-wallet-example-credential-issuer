# Regression Testing Spike — Document Builder (DCMAW-20629)

Note: we have used the following documentation to inform this spike

https://govukverify.atlassian.net/wiki/spaces/PLAT/pages/3054010402/How+to+run+tests+against+your+deployed+application+in+a+SAM+deployment+pipeline

https://govukverify.atlassian.net/wiki/spaces/PLAT/pages/3059908609/How+to+deploy+a+SAM+application+with+secure+pipelines


## Tool recommendation: Playwright

**Playwright** is the recommended tool for Document Builder regression tests.

- Installs as a dev dependency (`@playwright/test`) — no separate service or sidecar needed
- Downloads and manages its own browser binaries (Chromium, Firefox, Webkit)
- Works headless in CI and headed locally with no config change
- Has first-class TypeScript support
- Outputs JUnit XML natively, which the existing pipeline already consumes for test reporting
- Used and maintained by Microsoft; well-established in the GDS ecosystem

**Version used in this spike:** `@playwright/test@^1.65.1`

---

## What was built

Two tests are in `document-builder/regression-tests/tests/`:

### `start-journey.spec.ts`
Runs against the document-builder service alone (port 8001). No other services needed.

Covers:
1. Start page loads with correct title
2. "Start now" navigates to `/select-app`
3. App radio selection posts and redirects to `/select-document`
4. Document type radio selection (Driving Licence — `org.iso.18013.5.1.mDL`) redirects to `/build-driving-licence`
5. Form submits with pre-filled defaults and lands on `/view-credential-offer/:itemId`

### `document-number.spec.ts`
Requires the full stack (see below). Skips automatically with a clear message if the CRI is not running.

Covers:
1. The full journey above, then clicks "View credential" to reach `/view-credential`
2. Waits for the `jsontree.js` credential explorer component to render
3. Locates the document number using:
   ```
   [data-jsontree-js-path="nameSpaces\\org.iso.18013.5.1\\12\\elementValue"]
   ```
   Index 12 in the `org.iso.18013.5.1` namespace is the `document_number` element of a Driving Licence mDL credential.
4. Saves the value to `document-number.txt` for reuse across runs

---

## Running locally

```bash
# 1. Start local AWS emulators (from document-builder/)
npm run localAws:up

# 2. Start the app (from document-builder/)
npm run dev

# 3. Run the tests (from document-builder/regression-tests/)
npm test

# To see the browser during a run:
npm run test:headed
```

The start-journey test passes with only the document-builder running.
The document-number test requires the example-credential-issuer (port 8080) and
STS mock (port 9090) to also be running — see the example-credential-issuer README.

To verify the container locally before pushing:

```bash
cd document-builder/regression-tests

docker build -t pipeline-test:doc-builder .

docker run --rm \
  -e TEST_ENVIRONMENT=build \
  -e TEST_REPORT_ABSOLUTE_DIR=/tmp/out \
  -e CFN_DocBuilderFriendlyUrl=stub-credential-issuer.wallet-onboarding.build.account.gov.uk \
  pipeline-test:doc-builder
```

---

## Deployed environments

The document-builder is deployed to four environments across two AWS accounts,
as defined in `deploy/template.yaml` `EnvironmentVariables` mappings:

| Environment | AWS Account ID | App hostname |
|-------------|---------------|---------|
| `dev` | `219494607463` | `stub-credential-issuer.wallet-onboarding.dev.account.gov.uk` |
| `build` | `899032468188` | `stub-credential-issuer.wallet-onboarding.build.account.gov.uk` |
| `staging` | `899032468188` | `stub-credential-issuer.wallet-onboarding.staging.account.gov.uk` |
| `integration` | `899032468188` | `stub-credential-issuer.wallet-onboarding.integration.account.gov.uk` |

Dev has its own account. Build, staging, and integration share account `899032468188`,
confirmed by the ECR repository ARNs in the template mappings.

The SAM template exports a `DocBuilderFriendlyUrl` CloudFormation output whose value
is the bare hostname (no `https://` prefix). The pipeline passes this automatically to
the test container as `$CFN_DocBuilderFriendlyUrl`. `run-tests.sh` prepends `https://`
when constructing `BASE_URL` — no template changes are needed.

---

## ECR repositories in account 899032468188 (build)

| Repository | URI |
|---|---|
| `cdk-accel-container-assets-899032468188-eu-west-2` | `899032468188.dkr.ecr.eu-west-2.amazonaws.com/cdk-accel-container-assets-899032468188-eu-west-2` |
| `cred-issuer-image-repo-containerrepository-g8ydsvmkc2et` | `899032468188.dkr.ecr.eu-west-2.amazonaws.com/cred-issuer-image-repo-containerrepository-g8ydsvmkc2et` |
| `doc-builder-image-repo-containerrepository-edrvcvwivlcg` | `899032468188.dkr.ecr.eu-west-2.amazonaws.com/doc-builder-image-repo-containerrepository-edrvcvwivlcg` |
| `sts-mock-image-repo-containerrepository-c47qvuediwmo` | `899032468188.dkr.ecr.eu-west-2.amazonaws.com/sts-mock-image-repo-containerrepository-c47qvuediwmo` |

The CRI and STS mock both have image repos in the build account, confirming the full
stack is deployed there. `document-number.spec.ts` will run in build once the test
container is wired in.

There is **no test container ECR repository** in this account yet. One needs to be
provisioned — see Step 1 of the implementation plan below.

---

## Running in the SAM deployment pipeline

### How the Dev Platform test container model works

The GDS Dev Platform secure pipeline supports a dedicated test container that runs
**post-deploy** in each environment. The model is:

1. At the same time as the app image is built, a separate **test container image** is
   built and pushed to its own ECR repository
2. After the secure pipeline deploys the app to an environment, CodeBuild pulls the
   test image and runs it
3. The container receives all SAM stack outputs as `$CFN_<OUTPUT_NAME>` env vars —
   `$CFN_DocBuilderFriendlyUrl` is already available from the existing template output
4. The container must write JUnit XML to `$TEST_REPORT_ABSOLUTE_DIR` and exit
   non-zero on failure
5. The pipeline only proceeds to the next environment on a zero exit code

The pattern is established in this codebase — see `mobile-wallet-verifier-root-ca`
for a working reference implementation.

---

## Full implementation plan

### Step 1 — Provision the test ECR repository

The `mobile-wallet-verifier-root-ca` repo manages its pipeline infrastructure with
Terraform in `terraform/stacks/sam-pipelines/`. If the document-builder has an
equivalent Terraform stack, follow the same pattern by adding a
`container_repo_enabled = true` entry and setting
`test_image_repository_uri = "auto"` on the pipeline definition. Terraform will
create the ECR repo via the `container-image-repository` CloudFormation stack and
resolve the URI automatically:

```hcl
# In the build environment sam-pipelines.tfvars (account 899032468188)
pipelines = {
  "doc-builder-pipeline" = {
    enabled                = true
    sam_stack_name         = "wallet-doc-builder"
    github_repository_name = "mobile-wallet-example-credential-issuer"

    container_repo_enabled                      = true
    container_image_repository_name             = "doc-builder-test-image-repo-build"
    image_tag_mutable                           = false
    retained_image_count                        = "50"
    test_image_repository_uri                   = "auto"
    require_test_container_signature_validation = "Yes"
    test_report_format                          = "JUNITXML"

    include_promotion       = "Yes"
    slack_notification_type = "Failures"
  }
}
```

Note: on first apply set `test_image_repository_uri = "none"` while the ECR stack
is being created, then change to `"auto"` on the second apply — this is the pattern
documented in the verifier root CA tfvars comments.

If no Terraform stack exists for the document-builder pipeline infrastructure, the
ECR repository can be provisioned manually via CloudFormation as an interim using the
Dev Platform template (Step 7 of the secure pipeline guide).

### Step 2 — Add `regression-tests/Dockerfile`

The verifier root CA's Dockerfile uses an Alpine-based Node image, which is not
suitable here. Playwright's `--with-deps` flag only installs Chromium system
dependencies on Debian/Ubuntu via `apt-get` — it silently skips dependency
installation on Alpine, leaving Chromium unable to run. The Dockerfile must use a
Debian-based image instead:

```dockerfile
FROM node:22-bookworm-slim

LABEL org.opencontainers.image.title="Document Builder regression test image"
LABEL org.opencontainers.image.description="Playwright regression test image consumed by the Document Builder SAM pipeline Test action"
LABEL org.opencontainers.image.source="https://github.com/govuk-one-login/mobile-wallet-example-credential-issuer"

RUN adduser --disabled-password --gecos "" --home /regression-tests test

WORKDIR /regression-tests

COPY package.json package-lock.json ./
RUN npm ci --ignore-scripts --no-audit --no-fund

# Install Chromium and its OS-level dependencies (requires Debian/Ubuntu)
RUN npx playwright install chromium --with-deps

RUN apt-get install -y --no-install-recommends curl unzip \
    && curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip && ./aws/install \
    && rm -rf awscliv2.zip aws \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY playwright.config.ts ./
COPY tests/ ./tests/
COPY run-tests.sh /

RUN chmod +x /run-tests.sh && chown -R test:test /regression-tests

USER test

ENTRYPOINT ["/run-tests.sh"]
```

### Step 3 — Add `regression-tests/run-tests.sh`

Playwright's JUnit reporter writes the XML to the `outputFile` path set in
`playwright.config.ts`, not to stdout. The current config writes to
`../results/regression.xml` relative to the working directory `/regression-tests`,
which resolves to `/results/regression.xml` inside the container. `run-tests.sh`
copies from that path to `$TEST_REPORT_ABSOLUTE_DIR`:

```bash
#!/bin/bash
set -euo pipefail

# $CFN_DocBuilderFriendlyUrl is a bare hostname provided automatically by the
# pipeline from the DocBuilderFriendlyUrl CloudFormation output in deploy/template.yaml
export BASE_URL="https://${CFN_DocBuilderFriendlyUrl}"

run_tests() {
  cd /regression-tests
  npx playwright test
}

# Only run against build — staging and integration do not have the full stack
# available and the pipeline TestReportFormat would not be satisfied by an empty run.
# The test image is only pushed from the push-to-main.yml workflow which targets build.
if [[ "${TEST_ENVIRONMENT:-}" == "build" ]]; then
  if run_tests; then
    cp -rf /results "$TEST_REPORT_ABSOLUTE_DIR"
  else
    cp -rf /results "$TEST_REPORT_ABSOLUTE_DIR"
    exit 1
  fi
fi
```

Note: `playwright.config.ts` must set `outputFile` to an absolute path inside the
container or be updated so the report lands at a known location. The simplest fix is
to update `playwright.config.ts` to write to `/results/regression.xml` explicitly
when running inside a container, or to update `outputFile` to `./results/regression.xml`
(relative to the working directory `/regression-tests`) so the report is at
`/regression-tests/results/regression.xml` and the copy path matches.

### Step 4 — Add a reusable Docker build-and-push workflow

Modelled on `mobile-wallet-verifier-root-ca/.github/workflows/job_docker-build-and-push.yml`.
Add `.github/workflows/_reusable-docker-test-image.yml`:

```yaml
name: Docker Build + Push Test Image

on:
  workflow_call:
    inputs:
      WORKING_DIRECTORY:
        description: Path to the Docker build context
        required: true
        type: string
    secrets:
      GH_ACTIONS_ROLE_ARN:
        required: true
      ECR_REPOSITORY:
        required: true
      CONTAINER_SIGN_KMS_KEY:
        required: true

permissions:
  id-token: write
  contents: read

jobs:
  docker-build-push:
    name: Build and push test image to ECR
    runs-on: ubuntu-24.04
    defaults:
      run:
        working-directory: ${{ inputs.WORKING_DIRECTORY }}
    steps:
      - uses: actions/checkout@3d3c42e5aac5ba805825da76410c181273ba90b1 # v7.0.1

      - uses: aws-actions/configure-aws-credentials@e1253824e5c10ff9df46874f81ed3ec929e19cfd # v6.3.0
        with:
          aws-region: eu-west-2
          role-to-assume: ${{ secrets.GH_ACTIONS_ROLE_ARN }}

      - id: login-ecr
        uses: aws-actions/amazon-ecr-login@03f1aad4c6c7ffd436567f42f9384779290529bd # v2.1.7

      - uses: sigstore/cosign-installer@6f9f17788090df1f26f669e9d70d6ae9567deba6 # v4.1.2
        with:
          cosign-release: v2.5.2

      - name: Build, tag, push and sign test image
        env:
          COSIGN_YES: "true"
          ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
          ECR_REPOSITORY: ${{ secrets.ECR_REPOSITORY }}
          CONTAINER_SIGN_KMS_KEY: ${{ secrets.CONTAINER_SIGN_KMS_KEY }}
        run: |
          docker build -f Dockerfile -t "$ECR_REGISTRY/$ECR_REPOSITORY:latest" .
          docker push "$ECR_REGISTRY/$ECR_REPOSITORY:latest"
          cosign sign --key "awskms:///${CONTAINER_SIGN_KMS_KEY}" "$ECR_REGISTRY/$ECR_REPOSITORY:latest"
```

This workflow includes its own AWS authentication step. The reusable container deploy
workflow (`_reusable-container-deploy.yml`) also authenticates to AWS but as a
separate job — credentials are not shared across jobs in GitHub Actions.

### Step 5 — Call the workflow from push-to-main.yml

In `push-to-main.yml`, add a job alongside `document-builder-deploy` that builds
and pushes the test image. The test image is only pushed on merge to main (the same
trigger as the app image), so it is only available in the build environment. Add a
new GitHub secret `BUILD_DOC_BUILDER_TEST_ECR_REPOSITORY` once the ECR repo URI is
known from Step 1:

```yaml
  document-builder-test-image:
    name: Document Builder - Push test image
    needs: [detect-changes, document-builder-code-analysis]
    if: ${{ always() && needs.detect-changes.outputs.document-builder == 'true' && needs.document-builder-code-analysis.result == 'success' }}
    permissions:
      id-token: write
      contents: read
    uses: ./.github/workflows/_reusable-docker-test-image.yml
    with:
      WORKING_DIRECTORY: ./document-builder/regression-tests
    secrets:
      GH_ACTIONS_ROLE_ARN: ${{ secrets.BUILD_DOC_BUILDER_GH_ACTIONS_ROLE_ARN }}
      ECR_REPOSITORY: ${{ secrets.BUILD_DOC_BUILDER_TEST_ECR_REPOSITORY }}
      CONTAINER_SIGN_KMS_KEY: ${{ secrets.BUILD_DOC_BUILDER_CONTAINER_SIGNER_KMS_KEY_ARN }}
```

---

## Scope of tests per environment

| Environment | `start-journey.spec.ts` | `document-number.spec.ts` |
|-------------|------------------------|--------------------------|
| `build` | ✅ Runs | ✅ Runs (CRI deployed alongside) |
| `staging` | ⏭ Skips (`TEST_ENVIRONMENT` guard in `run-tests.sh`) | ⏭ Skips |
| `integration` | ⏭ Skips | ⏭ Skips |

The test image is only pushed automatically via `push-to-main.yml`, which targets
the build environment. Dev deployments are manual (`workflow_dispatch`) and no
test image push is triggered there, so the pipeline test action will not run in dev.

The skip in staging and integration is handled by the `TEST_ENVIRONMENT` guard in
`run-tests.sh`. Note that `start-journey.spec.ts` only requires the document-builder
itself — if tests are required in staging in future, `run-tests.sh` could be extended
to run that test there without the full-stack dependency.

---

## Open questions

1. **Terraform pipeline config** — does the document-builder already have a Terraform
   `sam-pipelines` stack like the verifier root CA? This determines whether Step 1
   is a Terraform change or a manual CloudFormation step.

2. **JUnit report path** — `playwright.config.ts` currently sets
   `outputFile: "../results/regression.xml"` which resolves to `/results/regression.xml`
   inside the container. Update this to `./results/regression.xml` before building the
   container so the report lands at `/regression-tests/results/regression.xml` and
   the `cp` in `run-tests.sh` works correctly.

3. **GitHub secret** — `BUILD_DOC_BUILDER_TEST_ECR_REPOSITORY` needs to be added to
   the repository secrets once the ECR repo URI is known from Step 1.

4. **`require_test_container_signature_validation`** — set to `"Yes"` in the tfvars
   above to match the verifier root CA pattern. This requires Cosign to sign the
   image in the GitHub Actions push step (Step 4/5), which is already included.

---

## Branch

`DCMAW-20629/document-builder-regression-tests`
