# FinSight Use Cases & Business Logic

FinSight is designed to handle modern personal finance workflows with an emphasis on automation, real-time alerts, and AI-driven insights. Below are the primary use cases supported by the platform.

---

## 1. Automated Transaction Processing & AI Categorization

### The Challenge
Manually entering and categorizing expenses is tedious and prone to user error. Traditional apps rely on rigid string-matching rules to guess a category (e.g., matching "Uber" to "Transport"), which fails on vague descriptions like "AMZN Mktp" or "Square Inc".

### The FinSight Workflow
1. **Import/Ingestion**: Users import transactions via CSV or external bank connections.
2. **AI Categorization**:
   - Instead of static rules, FinSight uses **Local LLMs (Ollama + Spring AI)** to semantically analyze the transaction description.
   - The AI considers context, utilizing vector embeddings of the user's historical transaction patterns (RAG) to accurately predict the category.
   - Because the AI runs locally, *no sensitive financial data is ever sent to third-party providers like OpenAI or Anthropic*.
3. **Audit Trail**: Every change to a transaction's category is recorded in an immutable audit log, allowing the user to trace exactly when and why an assignment was made.

---

## 2. Multi-Currency Wallets & Real-Time Exchange

### The Challenge
Users who travel frequently, live as digital nomads, or maintain international bank accounts struggle to view their true net worth in a single unified currency.

### The FinSight Workflow
1. **Wallet Creation**: A user creates multiple wallets (e.g., an Indian Rupee account and a US Dollar account).
2. **Transaction Recording**: When an expense is recorded in a foreign currency wallet (e.g., spending £50 GBP in London), FinSight automatically fetches the current exchange rate using a Resilience4j-backed API client.
3. **Unified Balances**: The user's dashboard seamlessly aggregates all wallet balances and transactions into their designated "Primary Wallet" currency (e.g., USD), utilizing highly precise `BigDecimal` math (8 decimal points of precision) for accurate conversions.

---

## 3. Intelligent Budgeting & Webhook Notifications

### The Challenge
Users often only realize they have overspent *after* they review their end-of-month statements. 

### The FinSight Workflow
1. **Setting Limits**: Users configure monthly limits for specific categories (e.g., $300 for "Dining Out").
2. **Threshold Monitoring**: As new transactions are added, the backend continually evaluates the category total against the budget limit.
3. **Event-Driven Alerts**: 
   - If spending hits a predefined threshold (e.g., 90%), an event is published to **Apache Kafka**.
   - The Kafka consumers process this event asynchronously, dispatching an in-app notification.
   - Users can also configure **External Webhooks**, allowing FinSight to send a secure, HMAC-signed JSON payload to their custom services (like a personal Discord server, Slack, or automation tool) the moment they breach a budget.

---

## 4. Automated Bank Reconciliation

### The Challenge
Ensuring that personal records match the bank's official statement is a manual, line-by-line verification process that wastes time.

### The FinSight Workflow
1. **Batch Upload**: Users upload an external bank statement snapshot.
2. **Reconciliation Engine**: FinSight processes the batch using a multi-step heuristic algorithm:
   - Identifies exact matches (same date, same exact amount).
   - Identifies partial matches (± 2 days, slight cent variance due to FX rates).
   - Flags missing transactions (recorded in FinSight but not in the bank, or vice-versa).
3. **Schedule**: Advanced users can configure scheduled reconciliation tasks that run as background cron jobs to continuously keep their ledgers synchronized.

---

## 5. Conversational AI Financial Advisor

### The Challenge
Standard dashboards provide raw data but do not offer actionable advice on how to improve financial health.

### The FinSight Workflow
1. **Vectorization**: FinSight continuously vectorizes the user's transaction history and budget status into a local vector database.
2. **RAG Chat**: The user opens the AI Chat widget and asks, *"Why did I overspend this month?"*
3. **Contextual Advice**: 
   - The system performs a similarity search over the user's specific data (strictly isolated by `user_id` to prevent cross-contamination).
   - The context is injected into a prompt for the `phi3` local LLM.
   - The AI responds with personalized advice, pointing out specific anomalous transactions or trend deviations that caused the budget breach.

---

## 6. Proactive Assistant via Telegram

### The Challenge
Users shouldn't have to log into a web dashboard every single day just to know where they stand on their spending.

### The FinSight Workflow
1. **Account Linking**: The user generates a secure, one-time 6-digit code in the FinSight settings and messages it to the FinSight Telegram Bot.
2. **Weekly Summaries**: A scheduled cron job (`TelegramNotificationScheduler`) pulls the user's monthly income, expenses, and balance, sending a beautifully formatted message to their Telegram every Sunday morning.
3. **Conversational Interface**: Since the Telegram bot is directly wired into the `AiService`, users can text the bot questions like *"How much have I spent on groceries this month?"* and get instant RAG-powered answers right on their phone.
