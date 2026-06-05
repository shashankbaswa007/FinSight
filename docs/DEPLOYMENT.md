# FinSight Deployment & Operations Guide

This guide provides instructions for deploying FinSight locally for development and in production environments.

---

## 1. Local Development (Docker Compose)

The easiest way to run the entire FinSight stack locally is via Docker Compose. This spins up the application, frontend, and all necessary dependencies without cluttering your host machine.

### Prerequisites
- Docker & Docker Compose installed
- Port `8080` (Backend API), `80` (Frontend), `3306` (MySQL), `6379` (Redis), and `9092` (Kafka) available.

### Run the Stack
```bash
# In the root of the FinSight repository
docker-compose up --build
```

### Accessing the Local Stack
- **Frontend App**: `http://localhost`
- **Backend API**: `http://localhost:8080/api`
- **Swagger Documentation**: `http://localhost:8080/swagger-ui.html`
- **Kafka UI (Debugging)**: `http://localhost:8081`

### Running the ELK Stack (Advanced Logging)
To run the project with centralized Elasticsearch, Logstash, and Kibana logging:
```bash
docker-compose -f docker-compose-elk.yml up --build
```
- **Kibana Dashboard**: `http://localhost:5601`

---

## 2. Production Deployment (AWS ECS / Fargate)

FinSight includes Infrastructure as Code (Terraform) scripts for deploying to AWS Elastic Container Service (ECS) using Fargate for serverless container management.

### Architecture Overview
- **VPC**: 2 Public Subnets (ALB, NAT) + 2 Private Subnets (ECS Tasks, RDS)
- **Database**: Amazon RDS for MySQL (Free Tier Eligible `db.t3.micro`)
- **Compute**: AWS Fargate (512 CPU, 1024 RAM per task)
- **Secrets Management**: AWS Systems Manager (SSM) Parameter Store

### Deployment Steps

1. **Configure AWS Credentials**
   Ensure you have the AWS CLI configured with `AdministratorAccess`.
   ```bash
   aws configure
   ```

2. **Push Docker Images to Amazon ECR**
   ```bash
   aws ecr create-repository --repository-name finsight-backend
   aws ecr create-repository --repository-name finsight-frontend

   # Tag and push your locally built images
   docker build -t finsight-backend .
   docker tag finsight-backend:latest <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/finsight-backend:latest
   docker push <AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/finsight-backend:latest
   ```

3. **Store Production Secrets in SSM**
   ```bash
   aws ssm put-parameter --name "db_password" --value "YourSecureDBPass!" --type "SecureString"
   aws ssm put-parameter --name "jwt_secret" --value "Base64EncodedSecureString==" --type "SecureString"
   aws ssm put-parameter --name "jasypt_password" --value "EncryptionKeyString" --type "SecureString"
   ```

4. **Initialize and Apply Terraform**
   ```bash
   cd infrastructure
   terraform init
   terraform plan -var="db_password=YourSecureDBPass!"
   terraform apply -var="db_password=YourSecureDBPass!"
   ```

### Post-Deployment Checklist
- **DNS Configuration**: Point your domain to the Application Load Balancer (ALB) output provided by Terraform.
- **SSL/TLS**: Ensure AWS Certificate Manager (ACM) is attached to the ALB listener for HTTPS termination.
- **Ollama AI Node**: Since Ollama requires significant memory and CPU, it is recommended to deploy it on a dedicated EC2 instance (e.g., `t3.large`) rather than serverless Fargate, pointing the Spring Boot application `spring.ai.ollama.base-url` property to the EC2 instance's internal IP.

---

## 3. Environment Variable Configuration

Below are the critical environment variables required when running the Spring Boot backend (`application-prod.properties`):

| Variable Name | Description | Example |
|---------------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Must be `prod` | `prod` |
| `DATABASE_URL` | JDBC Connection String | `jdbc:mysql://finsight-db.us-east-1.rds.amazonaws.com:3306/finsight` |
| `DATABASE_USERNAME` | RDS Username | `finsight_admin` |
| `DATABASE_PASSWORD` | RDS Password | `(Injected via SSM)` |
| `JWT_SECRET` | Base64-encoded 256-bit key for token generation | `dGhpcy...` |
| `JASYPT_ENCRYPTOR_PASSWORD` | Key for at-rest database encryption | `(Injected via SSM)` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka cluster URLs | `b-1.my-kafka.amazonaws.com:9092` |
| `REDIS_HOST` | Redis cache endpoint | `my-redis.cache.amazonaws.com` |
| `CORS_ALLOWED_ORIGINS` | Frontend URL | `https://app.finsight.com` |
