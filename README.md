# FinSight – Personal Finance Analytics Platform

A production-grade **full-stack fintech application** built with Java Spring Boot and React that helps users track transactions, manage budgets, analyze spending patterns, and receive anomaly alerts. Designed with banking-grade practices: versioned database migrations, idempotent API operations, immutable audit trails, and comprehensive test coverage.

---

## Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 17 (LTS) | Language |
| Spring Boot 3.4.3 | Application framework |
| Spring Data JPA | Database access (Hibernate ORM) |
| Spring Security | Authentication & authorization |
| JWT (jjwt 0.12.6) | Stateless token authentication |
| MySQL 8.0 | Relational database |
| Flyway | Versioned database migrations |
| Spring Validation | Input validation with Bean Validation |
| Spring Boot Actuator | Health checks & metrics |
| springdoc-openapi 2.8 | Swagger UI / OpenAPI 3.0 docs |
| Redis | Optional response caching |
| Maven | Build & dependency management |

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

## Security Considerations

- Passwords enforced: min 8 chars, uppercase, lowercase, digit, special character
- JWT secret configurable via `JWT_SECRET` environment variable
- Rate limiting prevents brute-force attacks (100 req/min per IP)
- All transaction mutations logged in immutable audit trail
- Sensitive endpoints (login, register, password change) masked in audit logs
- CORS restricted to configured frontend origin
- SQL injection prevented via parameterized JPA queries
- Input validation on all request DTOs

---

## License

MIT
