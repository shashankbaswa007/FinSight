<div align="center">
  <img src="https://raw.githubusercontent.com/shashankbaswa007/FinSight/main/docs/logo.png" alt="FinSight Logo" width="120" onerror="this.src='https://via.placeholder.com/120?text=FS'">
  
  # FinSight
  **Production-Grade Personal Finance Analytics & AI Advisor Platform**

  [![Java Support](https://img.shields.io/badge/Java-17_LTS-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://openjdk.java.net/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.14-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
  [![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
  [![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)
</div>

<br />

FinSight is a comprehensive, full-stack fintech application engineered to help users track transactions, manage budgets, and receive real-time, AI-driven financial insights. 

Built with **Java Spring Boot** and **React**, the platform is designed as a resume-ready showcase, strictly adhering to banking-grade engineering practices including distributed event-driven messaging, at-rest database cryptography, circuit-breaking resilience, and air-gapped machine learning.

## ✨ Key Features

- 🧠 **Local AI Integration (Spring AI + Ollama)**: Privacy-first transaction categorization and conversational financial advice using `phi3` and RAG (Retrieval-Augmented Generation).
- 📱 **Telegram Bot Integration**: A dedicated chatbot for requesting financial advice directly from your local AI and receiving proactive weekly transaction summaries.
- 🎨 **Neo-Brutalist UI**: A striking, accessible, and high-contrast user interface featuring dynamic dark mode and micro-animations.
- 🚀 **Bulk Excel Imports**: Support for processing up to 1000 transactions at a time via `.xlsx` files, complete with duplicate detection and error validation.
- 🔒 **Banking-Grade Security**: At-rest AES-256 encryption for sensitive transaction data via Jasypt, plus secure JWT authentication.
- ⚡ **Event-Driven Architecture**: Asynchronous webhook processing and audit logging backed by Apache Kafka and the Transactional Outbox pattern.
- 📊 **Rich Analytics**: Interactive Recharts visualizations for monthly trends, top spending categories, and budget tracking.
- ⚖️ **GDPR Compliance workflows**: Automated data exports and "Right to be Forgotten" hard-delete capabilities.

---

## 📖 Official Documentation

We have organized the documentation into comprehensive guides. Please refer to the `docs/` directory for detailed information:

1. 🎯 [**Use Cases & Business Logic**](docs/USE_CASES.md)
   Learn *what* FinSight does: AI Transaction Categorization, Multi-Currency Wallets, Webhook Budget Alerts, Automated Bank Reconciliation, and Conversational AI Advisors.

2. 🏗️ [**Architecture & Technical Design**](docs/ARCHITECTURE.md)
   Learn *how* FinSight works: Event-Driven Kafka architecture, the Transactional Outbox pattern, Jasypt Encryption, GDPR compliance workflows, and Local AI integration.

3. 🌐 [**API Reference**](docs/API_REFERENCE.md)
   Documentation of the RESTful API routes, required headers, authentication schemes, and Swagger UI access.

4. 🚀 [**Deployment Guide**](docs/DEPLOYMENT.md)
   Instructions for running the stack locally via Docker Compose, including the ELK stack for logging, as well as AWS ECS/Fargate Terraform production deployment guidelines.

---

## 🛠️ Technology Stack

| Domain | Technologies |
|---|---|
| **Backend** | Java 17 LTS, Spring Boot 3.5.x, Spring Security, Spring AI, Hibernate ORM |
| **Frontend** | React 18, TypeScript 5.6, Vite, Tailwind CSS, Recharts, Lucide Icons |
| **Databases & Cache** | MySQL 8.0, Redis 7 |
| **Messaging** | Apache Kafka (Confluent cp-kafka), Zookeeper |
| **Machine Learning** | Local Ollama (`phi3`, `nomic-embed-text`) |
| **Resilience & Security** | Resilience4j, Jasypt, JJWT, Flyway, Apache POI |

---

## ⚡ Quick Start (Local Development)

The fastest way to experience FinSight is by running the containerized stack locally.

### Prerequisites
- **Docker** and **Docker Compose** installed.
- **Local Ollama** installed and running on port `11434` with models `phi3` and `nomic-embed-text` pulled.
- Ensure ports `8080` (Backend), `80` (Frontend), `3306` (DB), `6379` (Redis), and `9092` (Kafka) are free.

### Run the Application

```bash
git clone https://github.com/shashankbaswa007/FinSight.git
cd FinSight

# Build and start the entire stack (Frontend, Backend, MySQL, Redis, Kafka)
docker-compose up --build
```

- **Frontend Interface**: [http://localhost](http://localhost) (or `http://localhost:5173` if running `npm run dev`)
- **Backend API Base**: `http://localhost:8080/api`
- **Swagger Documentation**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Demo Credentials
The database seeds itself with a demo user so you can explore the dashboard immediately:
- **Email**: `demo@finsight.com`
- **Password**: `Demo@1234`

---

## 📸 Screenshots

<details>
<summary><b>Click to view Screenshots</b></summary>
<br>
<i>Note: You can add actual screenshots of your dashboard, bulk upload modal, and AI chat interface here.</i>
</details>

---

## 🤝 Contributing
Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/shashankbaswa007/FinSight/issues).

## 🛡️ License
Distributed under the MIT License. See `LICENSE` for more information.
