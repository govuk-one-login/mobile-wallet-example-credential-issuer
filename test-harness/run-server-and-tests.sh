#!/bin/bash

set -e

bash run-server.sh &
SERVER_PID=$!

sleep 5

bash run-tests.sh
TEST_EXIT_CODE=$?

kill $SERVER_PID
wait $SERVER_PID 2>/dev/null || true

exit $TEST_EXIT_CODE
