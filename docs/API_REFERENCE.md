# FinSight API Reference

FinSight provides a secure, RESTful API. All endpoints (except registration and login) require a stateless JSON Web Token (JWT) provided in the `Authorization: Bearer <token>` header.

The API is fully documented via OpenAPI 3.0. When running locally, you can interact with the Swagger UI at: `http://localhost:8080/swagger-ui.html`.

---

## Global Headers
- `Authorization: Bearer <token>` (Required for protected endpoints)
- `X-Idempotency-Key: <uuid>` (Optional but recommended for `POST` requests to prevent duplicate processing)

---

## 1. Authentication (`/api/auth`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/register` | Register a new user. Returns a JWT token. |
| `POST` | `/login` | Authenticate an existing user. Returns a JWT token. |

---

## 2. Transactions (`/api/transactions`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | List all transactions with optional filters (`categoryId`, `type`, `startDate`, `endDate`). Paginated. |
| `POST` | `/` | Create a new transaction. Supports Idempotency keys. |
| `PUT` | `/{id}` | Update an existing transaction. |
| `DELETE` | `/{id}` | Delete a transaction (soft-delete in audit log). |
| `GET` | `/monthly` | Retrieve a high-level summary of income vs expenses for the current month. |

---

## 3. Analytics (`/api/analytics`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/monthly-summary` | Income, expenses, net savings, and savings ratio. |
| `GET` | `/top-categories` | Top spending categories sorted by amount. |
| `GET` | `/spending-trends` | Monthly income/expense bar chart data. |
| `GET` | `/anomaly-detection` | Returns flagged transactions based on Z-Score statistical variance. |
| `GET` | `/daily-spending` | Daily expense breakdown for line charts. |
| `GET` | `/category-trends` | Stacked data for category spending over multiple months. |
| `GET` | `/expense-distribution`| Bucketed spending analysis (e.g., $0-10, $10-50). |

---

## 4. Budgets & Notifications (`/api/budgets` & `/api/v1/notifications`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/budgets` | Set a monthly budget limit for a category. |
| `GET` | `/api/budgets/status` | View actual spending vs budgeted amount. |
| `GET` | `/api/v1/notifications`| Get all notifications (budget alerts, system alerts). |
| `PUT` | `/api/v1/notifications/{id}/read`| Mark a notification as read. |

---

## 5. Webhooks (`/api/v1/webhooks`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/` | Register a new webhook endpoint to receive real-time JSON payloads. |
| `GET` | `/` | List active webhooks. |
| `DELETE` | `/{id}` | Remove a webhook. |

---

## 6. Currencies & Wallets (`/api/v1/wallets` & `/api/v1/currencies`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/currencies`| Get a list of supported fiat currencies. |
| `POST` | `/api/v1/wallets` | Create a new wallet in a specific currency. |
| `GET` | `/api/v1/wallets` | Get all user wallets and their balances converted to the primary currency. |

---

## 7. Automated Reconciliation (`/api/v1/reconciliation`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/batches` | Initialize a new reconciliation batch. |
| `POST` | `/import-transactions`| Upload a JSON payload of external bank transactions to reconcile. |
| `POST` | `/batches/{id}/reconcile`| Trigger the AI/Heuristic matching engine for the batch. |
| `GET` | `/batches/{id}/matches` | Review Exact, Partial, and Unmatched results. |

---

## 8. Conversational AI (`/api/ai`)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/chat` | Send a natural language query to the AI advisor (e.g., "How can I reduce my dining expenses?"). The payload utilizes the RAG pipeline to return contextually accurate advice. |

---

## Standard Error Response Format
FinSight implements standard RFC 7807 problem details for HTTP APIs.

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Amount must be greater than zero",
  "path": "/api/transactions",
  "timestamp": "2024-03-05T10:30:00Z"
}
```
