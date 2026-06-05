#!/bin/bash

# ═══════════════════════════════════════════════════════════════════
# FinSight Load Testing Script
# Tests API endpoints under load with configurable parameters
# ═══════════════════════════════════════════════════════════════════

set -e

# Default values
BACKEND_URL="http://localhost:8080"
NUM_REQUESTS=1000
CONCURRENCY=10
ENDPOINT="/actuator/health"
OUTPUT_FILE="load-test-results.json"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

usage() {
  cat << EOF
Usage: ./load-test.sh [OPTIONS]

OPTIONS:
  -u, --url URL              Backend URL (default: http://localhost:8080)
  -n, --requests NUM         Number of requests (default: 1000)
  -c, --concurrency NUM      Concurrent requests (default: 10)
  -e, --endpoint PATH        API endpoint to test (default: /actuator/health)
  -o, --output FILE          Output results file (default: load-test-results.json)
  -h, --help                 Show this help message

EXAMPLES:
  ./load-test.sh -n 5000 -c 50 -e /api/transactions
  ./load-test.sh --url http://staging:8080 --requests 2000 --concurrency 100

EOF
  exit 0
}

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    -u|--url)
      BACKEND_URL="$2"
      shift 2
      ;;
    -n|--requests)
      NUM_REQUESTS="$2"
      shift 2
      ;;
    -c|--concurrency)
      CONCURRENCY="$2"
      shift 2
      ;;
    -e|--endpoint)
      ENDPOINT="$2"
      shift 2
      ;;
    -o|--output)
      OUTPUT_FILE="$2"
      shift 2
      ;;
    -h|--help)
      usage
      ;;
    *)
      echo "Unknown option: $1"
      usage
      ;;
  esac
done

# Verify Apache Bench is available
if ! command -v ab &> /dev/null; then
  echo "Error: Apache Bench (ab) not found. Install with:"
  echo "  macOS: brew install httpd"
  echo "  Linux: sudo apt-get install apache2-utils"
  exit 1
fi

echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  FinSight Load Testing${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""
echo "Configuration:"
echo "  URL: $BACKEND_URL"
echo "  Endpoint: $ENDPOINT"
echo "  Requests: $NUM_REQUESTS"
echo "  Concurrency: $CONCURRENCY"
echo "  Output: $OUTPUT_FILE"
echo ""

TARGET_URL="${BACKEND_URL}${ENDPOINT}"

# Verify backend is reachable
echo -e "${YELLOW}Checking backend connectivity...${NC}"
if ! curl -s -o /dev/null -w "%{http_code}" "$BACKEND_URL/actuator/health" | grep -q "200"; then
  echo -e "${YELLOW}Warning: Backend may not be responding correctly${NC}"
fi
echo ""

# Run load test
echo -e "${YELLOW}Running load test...${NC}"
echo "Target: $TARGET_URL"
echo "Starting: $(date)"
echo ""

# Run ab and capture output
ab_output=$(mktemp)
ab -n "$NUM_REQUESTS" -c "$CONCURRENCY" -q "$TARGET_URL" > "$ab_output" 2>&1 || true

# Parse results
requests_per_sec=$(grep "Requests per second" "$ab_output" | awk '{print $NF}')
time_per_request=$(grep "Time per request:" "$ab_output" | head -1 | awk '{print $NF}' | sed 's/\[ms\]//')
transfer_rate=$(grep "Transfer rate:" "$ab_output" | awk '{print $NF}')
failed_requests=$(grep "Failed requests:" "$ab_output" | awk '{print $NF}')
total_time=$(grep "Finished:" "$ab_output" | awk '{print $NF}')

# Extract connect times (if available)
connect_min=$(grep "Connect:" "$ab_output" | awk '{print $2}')
connect_mean=$(grep "Connect:" "$ab_output" | awk '{print $3}')
connect_max=$(grep "Connect:" "$ab_output" | awk '{print $5}')

echo ""
echo -e "${GREEN}Results:${NC}"
echo "  Requests/sec: $requests_per_sec"
echo "  Time per request: ${time_per_request} ms"
echo "  Transfer rate: $transfer_rate"
echo "  Failed requests: $failed_requests"
echo "  Total time: $total_time"
echo "  Timestamp: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
echo ""

# Display Apache Bench full output
echo -e "${YELLOW}Detailed Statistics:${NC}"
cat "$ab_output"
echo ""

# Save JSON results
echo -e "${YELLOW}Saving results to $OUTPUT_FILE...${NC}"
cat > "$OUTPUT_FILE" << EOF
{
  "test_config": {
    "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
    "backend_url": "$BACKEND_URL",
    "endpoint": "$ENDPOINT",
    "target_url": "$TARGET_URL",
    "total_requests": $NUM_REQUESTS,
    "concurrency": $CONCURRENCY
  },
  "results": {
    "requests_per_second": $requests_per_sec,
    "avg_time_per_request_ms": $time_per_request,
    "transfer_rate_kbps": "$transfer_rate",
    "failed_requests": $failed_requests,
    "total_time_seconds": $total_time
  },
  "system_info": {
    "java_version": "$(java -version 2>&1 | head -1)",
    "os": "$(uname -s)",
    "arch": "$(uname -m)"
  }
}
EOF

echo -e "${GREEN}✓ Load test complete${NC}"
echo ""
echo "Results saved to: $OUTPUT_FILE"
echo ""

# Cleanup
rm -f "$ab_output"

# Interpretation
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo "Performance Interpretation:"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""

# Convert to float for comparison
rps=$(echo "$requests_per_sec" | awk '{printf "%.0f", $1}')

if (( rps > 500 )); then
  echo -e "${GREEN}✓ Excellent throughput: $rps req/sec${NC}"
elif (( rps > 100 )); then
  echo -e "${GREEN}✓ Good throughput: $rps req/sec${NC}"
elif (( rps > 50 )); then
  echo -e "${YELLOW}⚠ Moderate throughput: $rps req/sec${NC}"
else
  echo -e "${YELLOW}⚠ Low throughput: $rps req/sec (investigate performance)${NC}"
fi

if [[ "$failed_requests" == "0" ]]; then
  echo -e "${GREEN}✓ Zero failed requests${NC}"
else
  echo -e "${YELLOW}⚠ $failed_requests failed requests detected${NC}"
fi

echo ""
echo "For comparison with previous results, see:"
echo "  - ./perf-results/*.json (performance benchmarks)"
echo "  - Git history for regression analysis"
echo ""
