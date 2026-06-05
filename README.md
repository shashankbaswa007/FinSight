# FinSight – Personal Finance Analytics Platform

A production-grade **full-stack fintech application** built with Java Spring Boot and React that helps users track transactions, manage budgets, analyze spending patterns, and receive anomaly alerts. Designed with banking-grade practices: versioned database migrations, idempotent API operations, immutable audit trails, and comprehensive test coverage.

---

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 25 (LTS, Sep 2034) | Language - Latest LTS with 8-year support |
| Spring Boot 3.5.12 | Application framework - Java 25 compatible, security hardened |
| Spring Data JPA | Database access (Hibernate ORM 6.6.18) |
| Spring Security | Authentication & authorization (with CVE fixes) |
| JWT (jjwt 0.12.6) | Stateless token authentication |
| MySQL 8.0 | Relational database |
| Flyway | Versioned database migrations |
| Spring Validation | Input validation with Bean Validation |
| Spring Boot Actuator | Health checks & metrics (authentication hardened) |
| springdoc-openapi 2.8 | Swagger UI / OpenAPI 3.0 docs |
| Redis | Optional response caching |
| Maven 3.9.12 | Build & dependency management |

### Frontend
| Technology | Purpose |
|---|---|
| React 18 | UI framework |
| TypeScript 5.6 | Type-safe JavaScript |
| Vite 6.0 | Build tool & dev server |
| Tailwind CSS 3.4 | Utility-first styling |
| Recharts 2.15 | Interactive charts & analytics |
| React Router v6 | Client-side routing |
| Lucide React | Icon library |

---

## Architecture

```
┌───────────────────────────────────────────────────────────┐
│                     React Frontend                         │
│  (TypeScript + Vite + Tailwind CSS + Recharts)            │
│  Pages: Dashboard, Transactions, Budgets, Analytics,      │
│         Recurring, Profile, Login/Register                │
└──────────────────────┬────────────────────────────────────┘
                       │ REST API (JSON over HTTPS)
                       ▼
┌───────────────────────────────────────────────────────────┐
│                  Spring Boot Backend                       │
│                                                           │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────────┐   │
│  │ Controllers  │→│  Services    │→│  Repositories  │   │
│  │ (REST API)  │  │ (Biz Logic)  │  │ (Spring Data)  │   │
│  └─────────────┘  └──────────────┘  └────────┬───────┘   │
│         ↑                                     │           │
│  ┌──────┴──────────────────────────┐          ▼           │
│  │ Security Layer                  │   ┌──────────────┐   │
│  │ • JWT Auth Filter               │   │  MySQL 8.0   │   │
│  │ • Rate Limiter (100 req/min)    │   │  (Flyway     │   │
│  │ • Audit Log Filter              │   │   managed)   │   │
│  │ • Idempotency Filter            │   └──────────────┘   │
│  └─────────────────────────────────┘                      │
└───────────────────────────────────────────────────────────┘
```

### Package Structure
```
src/main/java/com/finsight/
├── config/          # AuditLogFilter, CORS, DemoDataSeeder, OpenAPI, Redis
├── controller/      # 9 REST controllers (Auth, Transaction, Budget, Analytics, ...)
├── dto/             # 25+ request/response DTOs with validation
├── exception/       # GlobalExceptionHandler, custom exceptions
├── model/           # JPA entities, enums, audit trail
├── repository/      # Spring Data JPA repositories with custom queries
├── security/        # JWT, RateLimiting, Idempotency, SecurityConfig
├── service/         # 8 service classes (Transaction, Analytics, Export, ...)
└── util/            # SecurityUtil, DateUtil
```

---

## Database Schema

```
┌──────────┐       ┌──────────────┐       ┌────────────┐
│  users   │───1:N─│ transactions │───N:1─│ categories │
└──────────┘       └──────────────┘       └────────────┘
     │                    │                      │
     ├──1:N──┐            │               ┌─N:1──┘
     │       ▼            ▼               ▼
     │  ┌────────┐  ┌──────────────────┐
     │  │budgets │  │transaction_audit │  ← Immutable audit trail
     │  └────────┘  │      _log        │
     │              └──────────────────┘
     └──1:N──┐
             ▼
     ┌───────────────────┐    ┌───────────────────┐
     │recurring_          │    │ idempotency_keys  │  ← Dedup for POST requests
     │   transactions     │    └───────────────────┘
     └───────────────────┘
```

**Migrations:** Managed by Flyway (`V1__init_schema.sql`, `V2__transaction_audit_trail.sql`, `V3__idempotency_keys.sql`)

---

## API Endpoints (40+)

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register (name, email, password) |
| POST | `/api/auth/login` | Login → JWT token |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions` | Create transaction (supports `X-Idempotency-Key`) |
| PUT | `/api/transactions/{id}` | Update transaction |
| DELETE | `/api/transactions/{id}` | Delete transaction |
| GET | `/api/transactions` | List (paginated, filter by type/category/date) |
| GET | `/api/transactions/monthly` | Monthly income vs expense summary |

### Budgets
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/budgets` | Set a monthly budget |
| GET | `/api/budgets` | List all budgets |
| PUT | `/api/budgets/{id}` | Update a budget |
| DELETE | `/api/budgets/{id}` | Delete a budget |
| GET | `/api/budgets/status` | Budget vs actual spending |

### Analytics (9 endpoints)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analytics/monthly-summary` | Income, expenses, net savings, ratio |
| GET | `/api/analytics/top-categories` | Top spending categories |
| GET | `/api/analytics/spending-trends` | Monthly income/expense trends |
| GET | `/api/analytics/anomaly-detection` | Z-score anomaly detection |
| GET | `/api/analytics/month-over-month` | MoM income/expense comparison |
| GET | `/api/analytics/daily-spending` | Daily expense breakdown |
| GET | `/api/analytics/category-trends` | Category spending over months |
| GET | `/api/analytics/expense-distribution` | Amount-range distribution buckets |
| GET | `/api/analytics/top-descriptions` | Top merchants/descriptions |

### Recurring Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/recurring` | Create recurring transaction |
| GET | `/api/recurring` | List recurring transactions |
| PUT | `/api/recurring/{id}/deactivate` | Deactivate a recurring schedule |

### Profile Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile` | Get current user profile |
| PUT | `/api/profile` | Update name/email |
| PUT | `/api/profile/password` | Change password |

### Export
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/export/transactions/csv` | Export transactions to CSV |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/categories` | Create a category |
| GET | `/api/categories` | List all categories |

---

## Key Features

### Financial Management
- **Transaction CRUD** with pagination, multi-field filtering, and monthly summaries
- **Budget tracking** – set monthly limits per category with real-time overspend detection
- **Recurring transactions** – auto-generated daily/weekly/monthly/yearly via `@Scheduled`
- **CSV export** of transaction history with optional date range

### Analytics & Insights
- **9 analytics endpoints** covering every angle of personal finance
- **Anomaly detection** – z-score-based flagging with LOW/MEDIUM/HIGH severity
- **Month-over-month comparison** – income/expense change with percentages
- **Expense distribution** – bucketed spending analysis (₹0–500, ₹500–2K, etc.)
- **Category trends** – multi-month stacked category spending
- **Top merchants** – ranked by total spending amount

### Banking-Grade Security & Compliance
- **BCrypt** password hashing with complexity enforcement (uppercase, lowercase, digit, special char)
- **JWT** stateless authentication (24h expiry, configurable secret)
- **Rate limiting** – 100 requests/minute per IP
- **Idempotency keys** – `X-Idempotency-Key` header for safe POST retries
- **Immutable audit trail** – every transaction CREATE/UPDATE/DELETE is logged with old/new values, performer identity, and timestamp
- **Sensitive data masking** – auth/password endpoints masked in audit logs
- **CORS** configured for frontend origin

### Engineering Quality
- **Flyway migrations** – versioned, repeatable database schema management (no `ddl-auto=update`)
- **60+ unit & integration tests** – services, controllers, repositories, validation
- **Global exception handling** via `@ControllerAdvice` with structured error responses
- **OpenAPI 3.0 / Swagger UI** for interactive API exploration
- **Spring Boot Actuator** – health, info, and metrics endpoints
- **Demo data seeder** – 100+ transactions, 6 budgets, 4 recurring transactions for testing

---

## Local Setup

### Prerequisites

- **Java 17+** – `java -version`
- **Maven 3.9+** – `mvn -version`
- **MySQL 8.0+** – running on `localhost:3306`
- **Node.js 18+** – for the frontend (`node --version`)

### Backend Setup

**1. Set up MySQL:**

```bash
mysql -u root -p
CREATE DATABASE finsight;
CREATE USER 'finsight_user'@'localhost' IDENTIFIED BY 'finsight_pass';
GRANT ALL PRIVILEGES ON finsight.* TO 'finsight_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

**2. Run the backend:**

```bash
cd demo
./mvnw clean spring-boot:run
```

Flyway will automatically create all tables on first run.

**3. Access:**

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend available at `http://localhost:5173`

### Demo Account

The app starts with demo data pre-seeded (profile `demo`):

| Field | Value |
|-------|-------|
| Email | `demo@finsight.com` |
| Password | `Demo@1234` |

---

## Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw test jacoco:report
# Report at target/site/jacoco/index.html
```

### Test Coverage

| Test File | Tests | Type |
|-----------|-------|------|
| `TransactionServiceTest` | 5 | Unit (Mockito) |
| `BudgetServiceTest` | 5 | Unit (Mockito) |
| `CategoryServiceTest` | 5 | Unit (Mockito) |
| `AnalyticsServiceTest` | 12 | Unit (Mockito) |
| `RecurringTransactionServiceTest` | 9 | Unit (Mockito) |
| `ExportServiceTest` | 5 | Unit (Mockito) |
| `AuthControllerIntegrationTest` | 7 | Integration (MockMvc + H2) |
| `ProfileControllerIntegrationTest` | 6 | Integration (MockMvc + H2) |
| `InputValidationTest` | 12 | Integration (MockMvc + H2) |
| `TransactionRepositoryTest` | 4 | Repository (DataJpaTest + H2) |

Tests use **H2 in-memory database** (MySQL compatibility mode) with Flyway disabled.

---

## API Usage Examples

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "Secure@Pass1"
  }'
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "john@example.com",
  "name": "John Doe",
  "role": "USER"
}
```

### Create Transaction (with idempotency key)

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: txn-abc-123" \
  -d '{
    "amount": 450.50,
    "type": "EXPENSE",
    "categoryId": 1,
    "description": "Grocery shopping at DMart",
    "date": "2024-03-05"
  }'
```

### Anomaly Detection

```bash
curl http://localhost:8080/api/analytics/anomaly-detection \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

**Response:**
```json
[
  {
    "transactionId": 42,
    "amount": 15000.00,
    "categoryName": "Shopping",
    "description": "Electronics purchase",
    "date": "2024-03-01",
    "zScore": 3.45,
    "severity": "MEDIUM"
  }
]
```

### Error Response Format

All errors follow a consistent structure:

```json
{
  "status": 400,
  "message": "Amount must be positive",
  "timestamp": "2024-03-05T10:30:00"
}
```

---

## Prerequisites (Java 25 Upgrade - May 2026)

**⚠️ IMPORTANT**: As of May 2026, FinSight requires **Java 25 LTS** (or later compatible LTS version).

- **Java 25 LTS** – `java -version` should show `java version "25..."`
  - Download: https://adoptium.net/temurin/releases/ (Eclipse Adoptium)
  - LTS Support: Through September 2034 (8 years)
- **Maven 3.9.12+** – Compatible with Java 25 (included via mvnw wrapper)
- **MySQL 8.0+** – No changes required
- **Node.js 18+** – For frontend (no Java requirement)

---

## Docker Deployment (Java 25 LTS)

### Local Development with Docker Compose

```bash
# Build and run all services
docker-compose up --build

# Services running:
# - Backend:  http://localhost:8080
# - Frontend: http://localhost
# - MySQL:    localhost:3306
# - Redis:    localhost:6379
```

### Building Standalone Backend Image (Java 25)

```bash
# Build multi-stage Docker image (uses Java 25 LTS)
docker build -t finsight:java25 .

# Run with custom environment
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:mysql://host.docker.internal:3306/finsight \
  -e JWT_SECRET=your-secure-secret \
  finsight:java25
```

**Image Details:**
- **Base Image**: `eclipse-temurin:25-jre-alpine` (minimal, security-hardened)
- **Size**: ~300MB (lightweight JRE)
- **User**: Non-root `finsight` user for security
- **Health Check**: Configured via Spring Boot Actuator

---

## Staging Deployment Guide

### Staging Environment Setup

**Option 1: Cloud VMs (AWS EC2, GCP Compute, Azure VM)**

```bash
# Prerequisites on staging VM
sudo apt-get update
sudo apt-get install -y openjdk-25-jdk mysql-server

# Set Java 25 as default
sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-25-openjdk-amd64/bin/java 1

# Verify
java -version  # Should show Java 25
```

**Option 2: Kubernetes (K8s) Deployment**

Create `k8s-staging-deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: finsight-backend-staging
  labels:
    app: finsight
    environment: staging
spec:
  replicas: 2  # 2 replicas for staging
  selector:
    matchLabels:
      app: finsight
  template:
    metadata:
      labels:
        app: finsight
        environment: staging
    spec:
      containers:
      - name: backend
        image: finsight:java25  # Use Java 25 image
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          value: "jdbc:mysql://mysql-staging:3306/finsight?useSSL=true"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

Deploy to Kubernetes:

```bash
kubectl apply -f k8s-staging-deployment.yaml
kubectl get pods -l app=finsight
kubectl logs -f deployment/finsight-backend-staging
```

### Deployment Steps (VM-based)

1. **Build on CI/CD (GitHub Actions, Jenkins, GitLab CI)**:
   ```bash
   ./mvnw clean package -DskipTests -B
   docker build -t finsight:staging-java25 .
   docker push your-registry/finsight:staging-java25
   ```

2. **SSH into staging VM**:
   ```bash
   ssh -i staging-key.pem ubuntu@staging-server.example.com
   ```

3. **Deploy application**:
   ```bash
   # Stop old instance
   docker stop finsight-staging
   docker rm finsight-staging

   # Pull and run new image
   docker pull your-registry/finsight:staging-java25
   docker run -d \
     --name finsight-staging \
     -p 8080:8080 \
     -e SPRING_PROFILES_ACTIVE=prod \
     -e DATABASE_URL=jdbc:mysql://mysql-staging:3306/finsight?useSSL=true \
     -e JWT_SECRET=$(cat /secrets/jwt-secret) \
     your-registry/finsight:staging-java25
   ```

4. **Verify deployment**:
   ```bash
   curl -s http://localhost:8080/actuator/health | jq
   # Expected: {"status":"UP",...}

   curl -s http://localhost:8080/swagger-ui.html
   # API documentation accessible
   ```

5. **Run smoke tests**:
   ```bash
   # Register test user
   curl -X POST http://staging-server:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@staging.com","password":"Test@1234","fullName":"Staging Test"}'

   # Login
   curl -X POST http://staging-server:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"test@staging.com","password":"Test@1234"}'
   ```

---

## Performance Testing & Benchmarking (Java 25)

### Prerequisites

```bash
# Install Apache JMeter for load testing
brew install jmeter  # macOS
# or apt-get install jmeter  # Linux

# Install Apache Bench (simpler alternative)
brew install httpd  # macOS includes ab
```

### Simple Load Test (Apache Bench)

```bash
# Start backend with Java 25
./mvnw clean spring-boot:run

# Run 1000 requests with 50 concurrent
ab -n 1000 -c 50 http://localhost:8080/actuator/health

# Sample output expected:
# Requests per second:        [X] #/sec
# Time per request:            [Y] ms
# Failed requests:             0
```

### JMH Benchmark (Detailed Performance)

Create `src/main/java/com/finsight/benchmark/TransactionBenchmark.java`:

```java
package com.finsight.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import com.finsight.dto.TransactionRequestDTO;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Fork(value = 1, jvmArgs = {"-Xms1024m", "-Xmx2048m"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
public class TransactionBenchmark {

    private TransactionRequestDTO request;

    @Setup
    public void setup() {
        request = new TransactionRequestDTO();
        request.setAmount(new BigDecimal("5000.00"));
        request.setCategoryId(1L);
        request.setDescription("Test transaction");
        request.setDate(java.time.LocalDate.now());
        request.setType("EXPENSE");
    }

    @Benchmark
    public void benchmarkTransactionCreation() {
        // Test transaction creation throughput
        request.validate();
    }
}
```

Run with:

```bash
./mvnw jmh:benchmark
```

### Performance Test Script (Automated)

Create `scripts/performance-test.sh`:

```bash
#!/bin/bash

# FinSight Java 25 Performance Benchmark Script

echo "=== FinSight Performance Testing (Java 25 LTS) ==="
echo "Date: $(date)"
echo ""

# 1. Health check throughput (baseline)
echo "1. Health Endpoint Throughput (5000 requests, 100 concurrent):"
ab -n 5000 -c 100 -q http://localhost:8080/actuator/health

echo ""
echo "2. API Response Times:"
# Single request to measure response time
response_time=$(curl -s -w '%{time_total}' -o /dev/null http://localhost:8080/actuator/health)
echo "Health check response time: ${response_time}s"

echo ""
echo "3. Memory Usage:"
jps -l | grep finsight
jcmd $(jps -l | grep finsight | cut -d' ' -f1) GC.heap_dump filename=heap-java25.hprof

echo ""
echo "4. Java 25 Features Active:"
java -version
$JAVA_HOME/bin/java -XshowSettings:properties -version 2>&1 | grep -E "java.version|java.vm.name"

echo ""
echo "=== Performance Test Complete ==="
```

Run the test:

```bash
chmod +x scripts/performance-test.sh
./scripts/performance-test.sh
```

### Expected Performance Improvements (Java 17 → Java 25)

Based on JDK release notes:

| Metric | Expected Improvement | Notes |
|--------|---------------------|-------|
| Throughput (req/sec) | +5-10% | Better GC optimizations |
| Memory Usage | -8-12% | Compact strings, improved GC |
| Startup Time | -10-15% | CDS enhancements |
| Latency (p99) | -5-8% | Better JIT compilation |
| GC Pause Time | -20-30% | G1GC improvements |

### Comparing Baseline vs Java 25

```bash
# Run test twice: once with Java 17, once with Java 25
# Store results in CSV, compare:

echo "Java Version,Requests/sec,Avg Response (ms),Max Response (ms)" > perf_results.csv

# Test 1: Java 17 (if available)
# export JAVA_HOME=/path/to/java17
# ./mvnw clean spring-boot:run &
# ab -n 10000 -c 50 http://localhost:8080/actuator/health >> perf_results.csv

# Test 2: Java 25 (current)
# export JAVA_HOME=/path/to/java25
# ./mvnw clean spring-boot:run &
# ab -n 10000 -c 50 http://localhost:8080/actuator/health >> perf_results.csv

cat perf_results.csv
```

---

## Monitoring & Observability

### Spring Boot Actuator Endpoints

Available at `http://localhost:8080/actuator`:

- `/actuator/health` – Application health status
- `/actuator/metrics` – JVM, GC, memory metrics
- `/actuator/env` – Environment variables (prod profile)
- `/actuator/loggers` – Log level configuration
- `/actuator/threaddump` – Thread diagnostics

### Key Metrics to Monitor

```bash
# Monitor JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.gc.pause

# Monitor application metrics
curl http://localhost:8080/actuator/metrics/http.server.requests
```

---

## Security Considerations

- Passwords enforced: min 8 chars, uppercase, lowercase, digit, special character
- JWT secret configurable via `JWT_SECRET` environment variable
- Rate limiting prevents brute-force attacks (100 req/min per IP)
- All transaction mutations logged in immutable audit trail
- Sensitive endpoints (login, register, password change) masked in audit logs
- CORS restricted to configured frontend origin
- SQL injection prevented via parameterized JPA queries
- Input validation on all request DTOs
- **Java 25 LTS**: Security updates guaranteed through September 2034

---

## License

MIT
