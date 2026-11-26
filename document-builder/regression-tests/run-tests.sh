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
# available. The test image is only pushed from push-to-main.yml which targets build.
if [[ "${TEST_ENVIRONMENT:-}" == "build" ]]; then
  if run_tests; then
    cp -rf /regression-tests/results "$TEST_REPORT_ABSOLUTE_DIR"
  else
    cp -rf /regression-tests/results "$TEST_REPORT_ABSOLUTE_DIR"
    exit 1
  fi
fi
