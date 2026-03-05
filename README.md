# FinSight – Personal Finance Analytics Platform

A production-grade **fintech backend system** built with Java Spring Boot that helps users manage personal finances by tracking transactions, budgets, and financial insights. All components are free, open-source, and runnable locally.

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 (LTS) | Language |
| Spring Boot 3.4 | Application framework |
| Spring Data JPA | Database access |
| Spring Security | Authentication & authorization |
| JWT (jjwt 0.12) | Stateless token auth |
| MySQL 8.0 | Relational database |
| Hibernate | ORM |
| Maven | Build tool |
| Lombok | Boilerplate reduction |
| Spring Validation | Input validation |
| Spring Boot Actuator | Monitoring |
| springdoc-openapi | Swagger UI |
| Redis | Optional caching |
| Docker & Compose | Containerized deployment |

---

## Architecture

```
src/main/java/com/finsight/
├── config/          # OpenAPI, Redis configuration
├── controller/      # REST API endpoints
├── dto/             # Request/response data transfer objects
├── exception/       # Global exception handling
├── model/           # JPA entity classes & enums
├── repository/      # Spring Data JPA repositories
├── security/        # JWT provider, filter, security config
├── service/         # Business logic layer
└── util/            # Helper utilities
```

**Flow:** Controller → Service → Repository → Database

---

## Database Schema

```
┌──────────┐       ┌──────────────┐       ┌────────────┐
│  users   │───1:N─│ transactions │───N:1─│ categories │
└──────────┘       └──────────────┘       └────────────┘
     │                                          │
     └──────1:N────┐                   ┌───N:1──┘
                   │                   │
              ┌────────────┐
              │  budgets   │
              └────────────┘
```

---

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions` | Add a transaction |
| PUT | `/api/transactions/{id}` | Update a transaction |
| DELETE | `/api/transactions/{id}` | Delete a transaction |
| GET | `/api/transactions` | List (paginated, filterable) |
| GET | `/api/transactions/monthly` | Monthly summary |

### Categories
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/categories` | Create a category |
| GET | `/api/categories` | List all categories |

### Budgets
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/budgets` | Set a monthly budget |
| GET | `/api/budgets` | List all budgets |
| GET | `/api/budgets/status` | Budget vs actual spending |

### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analytics/monthly-summary` | Income, expenses, savings |
| GET | `/api/analytics/top-categories` | Top spending categories |
| GET | `/api/analytics/spending-trends` | Monthly trends (last N months) |
| GET | `/api/analytics/anomaly-detection` | Z-score anomaly detection |

---

## Local Setup

### Prerequisites

- **Java 21+** – `java -version`
- **Maven 3.9+** – `mvn -version`
- **MySQL 8.0+** – running on `localhost:3306`
- **Docker** (optional) – for containerized setup

### Option 1: Run with Docker Compose (Recommended)

```bash
# Build the application
./mvnw clean package -DskipTests

# Start MySQL, Redis, and the application
docker-compose up -d

# App available at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
```

### Option 2: Run Locally

**1. Set up MySQL:**

```bash
# Create database and user
mysql -u root -p
CREATE DATABASE finsight;
CREATE USER 'finsight_user'@'localhost' IDENTIFIED BY 'finsight_pass';
GRANT ALL PRIVILEGES ON finsight.* TO 'finsight_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

**2. Configure application:**

Edit `src/main/resources/application.properties` if your DB credentials differ.

**3. Run the application:**

```bash
./mvnw clean spring-boot:run
```

**4. Access:**

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`

---

## API Usage Examples

### Register

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "securePass123"
  }'
```

**Response:**
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

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "securePass123"
  }'
```

### Add Transaction

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 45.50,
    "type": "EXPENSE",
    "categoryId": 1,
    "description": "Grocery shopping",
    "date": "2026-03-05"
  }'
```

### Get Monthly Summary

```bash
curl http://localhost:8080/api/analytics/monthly-summary?month=3&year=2026 \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

**Response:**
```json
{
  "month": 3,
  "year": 2026,
  "totalIncome": 5000.00,
  "totalExpense": 2350.75,
  "netSavings": 2649.25,
  "incomeExpenseRatio": 2.13
}
```

### Budget Status

```bash
curl "http://localhost:8080/api/budgets/status?month=3&year=2026" \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

**Response:**
```json
[
  {
    "budgetId": 1,
    "categoryName": "Food",
    "monthlyLimit": 500.00,
    "amountSpent": 320.50,
    "remaining": 179.50,
    "exceeded": false,
    "month": 3,
    "year": 2026
  }
]
```

---

## Security

- **BCrypt** password hashing
- **JWT** stateless authentication (24h expiry)
- Role-based authorization (`USER`, `ADMIN`)
- All financial data endpoints require a valid JWT
- Custom `AuthenticationEntryPoint` for JSON 401 responses

---

## Key Features

- **Paginated transactions** with category and date range filters
- **Monthly budget tracking** with overspend detection
- **Financial analytics** – income/expense ratios, top categories, trends
- **Anomaly detection** – z-score based identification of unusual spending
- **Input validation** with meaningful error messages
- **Global exception handling** via `@ControllerAdvice`
- **Swagger UI** for interactive API exploration
- **Docker Compose** for one-command setup

---

## License

MIT
