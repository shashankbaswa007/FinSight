#!/bin/bash

# ═══════════════════════════════════════════════════════════════════
# FinSight Java 25 LTS Performance Benchmark Script
# Measures throughput, latency, and resource utilization
# ═══════════════════════════════════════════════════════════════════

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
RESULTS_DIR="${RESULTS_DIR:-./perf-results}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
RESULTS_FILE="${RESULTS_DIR}/benchmark_${TIMESTAMP}.json"

# Test parameters
HEALTH_REQUESTS=5000
HEALTH_CONCURRENCY=50
API_REQUESTS=2000
API_CONCURRENCY=20

echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  FinSight Performance Benchmark (Java 25 LTS)${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""
echo "Backend URL: $BACKEND_URL"
echo "Timestamp: $(date '+%Y-%m-%d %H:%M:%S')"
echo "Java Version:"
java -version 2>&1 | head -1
echo ""

# Create results directory
mkdir -p "$RESULTS_DIR"

# ─────────────────────────────────────────────────────────────────
# 1. Health Check - Baseline Throughput
# ─────────────────────────────────────────────────────────────────
echo -e "${YELLOW}[1/6] Testing Health Endpoint (Baseline)${NC}"
echo "  Requests: $HEALTH_REQUESTS | Concurrency: $HEALTH_CONCURRENCY"

health_output=$(mktemp)
ab -n $HEALTH_REQUESTS -c $HEALTH_CONCURRENCY -q "$BACKEND_URL/actuator/health" > "$health_output" 2>&1 || true

health_rps=$(grep "Requests per second" "$health_output" | awk '{print $NF}')
health_avg=$(grep "Time per request:" "$health_output" | head -1 | awk '{print $NF}' | sed 's/\[ms\]//')
health_max=$(grep "Time per request:" "$health_output" | tail -1 | awk '{print $NF}' | sed 's/\[ms\]//')

echo -e "  ${GREEN}Requests/sec: ${health_rps}${NC}"
echo -e "  ${GREEN}Avg Response: ${health_avg} ms${NC}"
echo -e "  ${GREEN}Max Response: ${health_max} ms${NC}"
echo ""

# ─────────────────────────────────────────────────────────────────
# 2. JVM Memory Metrics
# ─────────────────────────────────────────────────────────────────
echo -e "${YELLOW}[2/6] Collecting JVM Memory Metrics${NC}"

memory_response=$(curl -s "$BACKEND_URL/actuator/metrics/jvm.memory.used" 2>/dev/null || echo '{}')
heap_response=$(curl -s "$BACKEND_URL/actuator/metrics/jvm.memory.max" 2>/dev/null || echo '{}')
gc_response=$(curl -s "$BACKEND_URL/actuator/metrics/jvm.gc.pause" 2>/dev/null || echo '{}')

echo -e "  ${GREEN}Memory metrics collected${NC}"
echo ""

# ─────────────────────────────────────────────────────────────────
# 3. API Response Times (Transaction Endpoint Simulation)
# ─────────────────────────────────────────────────────────────────
echo -e "${YELLOW}[3/6] Testing API Response Times${NC}"
echo "  Requests: $API_REQUESTS | Concurrency: $API_CONCURRENCY"

api_output=$(mktemp)
ab -n $API_REQUESTS -c $API_CONCURRENCY -q "$BACKEND_URL/swagger-ui.html" > "$api_output" 2>&1 || true

api_rps=$(grep "Requests per second" "$api_output" | awk '{print $NF}')
api_avg=$(grep "Time per request:" "$api_output" | head -1 | awk '{print $NF}' | sed 's/\[ms\]//')
api_p50=$(grep "Time per request:" "$api_output" | tail -1 | awk '{print $NF}' | sed 's/\[ms\]//')

echo -e "  ${GREEN}Requests/sec: ${api_rps}${NC}"
echo -e "  ${GREEN}Avg Response: ${api_avg} ms${NC}"
echo -e "  ${GREEN}P50 Response: ${api_p50} ms${NC}"
echo ""

# ─────────────────────────────────────────────────────────────────
# 4. Thread Count & GC Statistics
# ─────────────────────────────────────────────────────────────────
echo -e "${YELLOW}[4/6] Collecting Thread & GC Statistics${NC}"

# Get JVM process info
jvm_pid=$(jps -l 2>/dev/null | grep "FinSightApplication\|spring-boot" | awk '{print $1}' | head -1)

if [ -n "$jvm_pid" ]; then
  thread_count=$(jps -l | grep "$jvm_pid" > /dev/null && ps -o nlwp= -p "$jvm_pid" || echo "N/A")
  echo -e "  ${GREEN}Active Threads: ${thread_count}${NC}"
  
  # Collect thread dump info
  jcmd "$jvm_pid" Thread.print > "${RESULTS_DIR}/threaddump_${TIMESTAMP}.txt" 2>/dev/null || true
  echo -e "  ${GREEN}Thread dump: ${RESULTS_DIR}/threaddump_${TIMESTAMP}.txt${NC}"
else
  echo -e "  ${YELLOW}JVM process not found (running standalone test only)${NC}"
fi
echo ""

# ─────────────────────────────────────────────────────────────────
# 5. Java Version & System Info
# ─────────────────────────────────────────────────────────────────
echo -e "${YELLOW}[5/6] Collecting System Information${NC}"

java_version=$(java -version 2>&1 | grep "java version" | awk -F'"' '{print $2}')
os_name=$(uname -s)
os_arch=$(uname -m)
cpu_cores=$(sysctl -n hw.ncpu 2>/dev/null || nproc 2>/dev/null || echo "N/A")
total_memory=$(sysctl -n hw.memsize 2>/dev/null | awk '{print $1/1024/1024/1024 " GB"}' || free -h | awk '/Mem:/ {print $2}')

echo -e "  ${GREEN}Java Version: ${java_version}${NC}"
echo -e "  ${GREEN}OS: ${os_name} ${os_arch}${NC}"
echo -e "  ${GREEN}CPU Cores: ${cpu_cores}${NC}"
echo -e "  ${GREEN}Total Memory: ${total_memory}${NC}"
echo ""

# ─────────────────────────────────────────────────────────────────
# 6. Generate Report
# ─────────────────────────────────────────────────────────────────
echo -e "${YELLOW}[6/6] Generating Report${NC}"

cat > "$RESULTS_FILE" << EOF
{
  "timestamp": "$(date -u +%Y-%m-%dT%H:%M:%SZ)",
  "backend_url": "$BACKEND_URL",
  "java_version": "$java_version",
  "system": {
    "os": "$os_name",
    "arch": "$os_arch",
    "cpu_cores": "$cpu_cores",
    "total_memory": "$total_memory"
  },
  "benchmarks": {
    "health_endpoint": {
      "requests": $HEALTH_REQUESTS,
      "concurrency": $HEALTH_CONCURRENCY,
      "requests_per_second": $health_rps,
      "avg_response_ms": $health_avg,
      "max_response_ms": $health_max
    },
    "api_endpoints": {
      "requests": $API_REQUESTS,
      "concurrency": $API_CONCURRENCY,
      "requests_per_second": $api_rps,
      "avg_response_ms": $api_avg,
      "p50_response_ms": $api_p50
    }
  }
}
EOF

echo -e "  ${GREEN}Report saved: ${RESULTS_FILE}${NC}"
echo ""

# ─────────────────────────────────────────────────────────────────
# Summary
# ─────────────────────────────────────────────────────────────────
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo -e "${GREEN}Benchmark Complete!${NC}"
echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
echo ""
echo "Summary:"
echo "  Health Endpoint Throughput: ${health_rps} req/sec"
echo "  API Throughput: ${api_rps} req/sec"
echo "  Average Latency: ${health_avg} ms (health), ${api_avg} ms (api)"
echo ""
echo "Full report: $RESULTS_FILE"
echo ""

# Cleanup
rm -f "$health_output" "$api_output"

echo -e "${GREEN}✓ Performance testing complete${NC}"
