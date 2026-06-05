# FinSight – Personal Finance Analytics Platform

FinSight is a production-grade, full-stack fintech application designed to help users track transactions, manage budgets, automate reconciliation, and receive AI-driven financial insights. 

Built with **Java Spring Boot** and **React**, the platform strictly adheres to banking-grade engineering practices including distributed event-driven messaging, at-rest database cryptography, circuit-breaking resilience, and air-gapped machine learning.

![FinSight Architecture](docs/architecture-overview.png) <!-- Replace with actual image later -->

---

## 📖 Official Documentation

We have organized the documentation into comprehensive guides. Please refer to the `docs/` directory for detailed information:

1. 🎯 [**Use Cases & Business Logic**](docs/USE_CASES.md)
   Learn *what* FinSight does: AI Transaction Categorization, Multi-Currency Wallets, Webhook Budget Alerts, Automated Bank Reconciliation, and Conversational AI Advisors.

2. 🏗️ [**Architecture & Technical Design**](docs/ARCHITECTURE.md)
   Learn *how* FinSight works: Event-Driven Kafka architecture, the Transactional Outbox pattern, Jasypt Encryption, GDPR compliance workflows, and Local AI (Ollama + Spring AI) integration.

3. 🌐 [**API Reference**](docs/API_REFERENCE.md)
   Documentation of the RESTful API routes, required headers, authentication schemes, and Swagger UI access.

4. 🚀 [**Deployment Guide**](docs/DEPLOYMENT.md)
   Instructions for running the stack locally via Docker Compose, including the ELK stack for logging, as well as AWS ECS/Fargate Terraform production deployment guidelines.

---

## ⚡ Quick Start (Local Development)

The fastest way to experience FinSight is by running the containerized stack locally.

### Prerequisites
- **Docker** and **Docker Compose** installed.
- Ensure ports `8080`, `80`, `3306`, `6379`, and `9092` are free.

### Run the Application

```bash
git clone https://github.com/shashankbaswa007/FinSight.git
cd FinSight

# Build and start the entire stack (Frontend, Backend, MySQL, Redis, Kafka)
docker-compose up --build
```

- **Frontend Interface**: [http://localhost](http://localhost)
- **Backend API Base**: `http://localhost:8080/api`
- **Swagger Documentation**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

### Demo Credentials
The database seeds itself with a demo user so you can explore the dashboard immediately:
- **Email**: `demo@finsight.com`
- **Password**: `Demo@1234`

---

## 🛠️ Technology Stack Highlight

- **Backend**: Java 17 LTS, Spring Boot 3.5.x, Spring Security, Spring AI, Hibernate ORM
- **Frontend**: React 18, TypeScript 5.6, Vite, Tailwind CSS, Recharts
- **Databases & Cache**: MySQL 8.0, Redis 7
- **Event Streaming**: Apache Kafka (Confluent cp-kafka)
- **Local AI**: Ollama (`phi3`, `nomic-embed-text`)
- **Resilience & Security**: Resilience4j, Jasypt, JJWT, Flyway

---

## 🛡️ License
MIT License
