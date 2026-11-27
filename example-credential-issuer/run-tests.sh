#!/bin/bash
# Run the existing tests for the example credential issuer.
# Exits 0 if all tests pass, non-zero on any test failure or error.
set -euo pipefail

cd "$(dirname "$0")"

# exec replaces the shell so Gradle's exit code is returned unchanged
exec ./gradlew test "$@"
