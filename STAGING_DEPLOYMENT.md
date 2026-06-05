# FinSight Staging Deployment Guide (Java 25 LTS)

**Last Updated**: May 2, 2026  
**Java Version**: 25 LTS (Sep 2034 support)  
**Spring Boot**: 3.5.12 (Security hardened)  

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Pre-Deployment Checklist](#pre-deployment-checklist)
3. [Deployment Options](#deployment-options)
4. [VM-Based Deployment](#vm-based-deployment)
5. [Kubernetes Deployment](#kubernetes-deployment)
6. [Docker Compose (Staging)](#docker-compose-staging)
7. [Post-Deployment Verification](#post-deployment-verification)
8. [Rollback Procedure](#rollback-procedure)
9. [Monitoring](#monitoring)
10. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### System Requirements

| Component | Minimum | Recommended | Notes |
|-----------|---------|-------------|-------|
| Java | 25 LTS | 25.0.2+ | Use eclipse-temurin or adoptium |
| Maven | 3.9.12 | 3.9.12+ | (Included via mvnw wrapper) |
| MySQL | 8.0.20+ | 8.0.36+ | InnoDB required |
| Redis | 6.0+ | 7.0+ | Optional, for caching |
| Docker | 20.10+ | Latest | For containerized deployment |
| Kubernetes | 1.24+ | 1.28+ | (K8s deployment only) |

### Network Requirements

- **Inbound**: Port 8080 (backend API), Port 80/443 (frontend)
- **Outbound**: Port 443 (HTTPS), Port 3306 (MySQL), Port 6379 (Redis)
- **Firewall**: Configure to allow backend-to-database communication
- **DNS**: Configure `staging-backend.example.com` CNAME record

### Java 25 Installation

```bash
# macOS (Homebrew)
brew install openjdk@25
export JAVA_HOME=/opt/homebrew/opt/openjdk@25

# Linux (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install -y openjdk-25-jdk
sudo update-alternatives --install /usr/bin/java java /usr/lib/jvm/java-25-openjdk-amd64/bin/java 1

# Verify
java -version
# Expected: openjdk version "25" 2026-01-20

mvn -version
# Expected: Maven 3.9.12 (via mvnw wrapper)
```

---

## Pre-Deployment Checklist

Before deploying to staging, verify:

- [ ] **Code reviewed** and merged to `main` branch
- [ ] **All tests passing** locally: `./mvnw clean test` (71/71 passing)
- [ ] **Docker image built**: `docker build -t finsight:staging .`
- [ ] **Security scan completed**: `./mvnw dependency-check:check` (zero HIGH/CRITICAL CVEs)
- [ ] **Java 25 verified**: `java -version` returns Java 25
- [ ] **Environment variables prepared** (JWT_SECRET, DATABASE credentials)
- [ ] **Database backups** created (if updating schema)
- [ ] **Rollback plan** documented
- [ ] **Stakeholders notified** of deployment window

---

## Deployment Options

### Option 1: VM-Based Deployment (Recommended for Staging)
- **Effort**: Low
- **Scalability**: Manual (1-2 instances)
- **Cost**: Low
- **Best for**: Staging, small-scale testing
- **Time**: ~15 minutes

### Option 2: Kubernetes Deployment
- **Effort**: Medium
- **Scalability**: Auto-scaling ready
- **Cost**: Medium
- **Best for**: Large-scale staging, production-ready
- **Time**: ~30 minutes

### Option 3: Docker Compose (Local Staging)
- **Effort**: Very Low
- **Scalability**: Single-host
- **Cost**: Free (local)
- **Best for**: Developer testing, demo
- **Time**: ~5 minutes

---

## VM-Based Deployment

### Step 1: Prepare Staging VM

```bash
# SSH into staging server
ssh -i staging-key.pem ubuntu@staging-backend.example.com

# Update system packages
sudo apt-get update && sudo apt-get upgrade -y

# Install Java 25
sudo apt-get install -y openjdk-25-jdk docker.io

# Add ubuntu user to docker group (avoid sudo)
sudo usermod -aG docker ubuntu
newgrp docker

# Create application directory
mkdir -p /app/finsight
cd /app/finsight
```

### Step 2: Configure Credentials

Create `.env` file (never commit to git):

```bash
cat > /app/finsight/.env << 'EOF'
# Spring Boot Configuration
SPRING_PROFILES_ACTIVE=prod
SPRING_APPLICATION_NAME=finsight-staging

# Database
DATABASE_URL=jdbc:mysql://mysql-staging.internal:3306/finsight_staging?useSSL=true&serverTimezone=UTC
DATABASE_USERNAME=finsight_staging_user
DATABASE_PASSWORD=<generate-random-password>

# JWT Secret (min 64 chars, base64)
JWT_SECRET=$(openssl rand -base64 48)

# CORS & Security
CORS_ALLOWED_ORIGINS=https://staging-frontend.example.com,http://localhost:3000
CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_ALLOW_CREDENTIALS=true

# Redis (optional caching)
SPRING_CACHE_TYPE=redis
SPRING_DATA_REDIS_HOST=redis-staging.internal
SPRING_DATA_REDIS_PORT=6379

# Logging
LOGGING_LEVEL_COM_FINSIGHT=INFO
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=WARN

# Server
SERVER_PORT=8080
SERVER_SERVLET_CONTEXT_PATH=/api
EOF

chmod 600 /app/finsight/.env
```

### Step 3: Build & Push Docker Image

From your local machine or CI/CD pipeline:

```bash
# Build Docker image (Java 25)
./mvnw clean package -DskipTests -B
docker build -t finsight:staging-java25 .

# Tag for registry
docker tag finsight:staging-java25 your-registry/finsight:staging-java25

# Push to container registry
docker login your-registry
docker push your-registry/finsight:staging-java25
```

### Step 4: Deploy to Staging VM

```bash
# SSH into staging server
ssh -i staging-key.pem ubuntu@staging-backend.example.com

# Pull latest image
docker pull your-registry/finsight:staging-java25

# Stop old container
docker stop finsight-staging 2>/dev/null || true
docker rm finsight-staging 2>/dev/null || true

# Run new container
docker run -d \
  --name finsight-staging \
  --restart unless-stopped \
  -p 8080:8080 \
  --env-file /app/finsight/.env \
  -v /app/finsight/logs:/app/logs \
  --health-cmd="curl -f http://localhost:8080/actuator/health || exit 1" \
  --health-interval=30s \
  --health-timeout=10s \
  --health-retries=3 \
  your-registry/finsight:staging-java25

# View logs
docker logs -f finsight-staging
```

---

## Kubernetes Deployment

### Step 1: Create Kubernetes Manifests

**Namespace**:
```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: finsight-staging
```

**ConfigMap** (non-sensitive):
```yaml
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: finsight-config
  namespace: finsight-staging
data:
  SPRING_PROFILES_ACTIVE: "prod"
  LOGGING_LEVEL_COM_FINSIGHT: "INFO"
  CORS_ALLOWED_METHODS: "GET,POST,PUT,DELETE,OPTIONS"
```

**Secret** (sensitive credentials):
```yaml
# k8s/secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: finsight-secrets
  namespace: finsight-staging
type: Opaque
stringData:
  DATABASE_USERNAME: "finsight_staging_user"
  DATABASE_PASSWORD: "your-secure-password"
  JWT_SECRET: "your-base64-encoded-secret"
```

**Deployment**:
```yaml
# k8s/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: finsight-backend
  namespace: finsight-staging
  labels:
    app: finsight
    environment: staging
spec:
  replicas: 2
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
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
        image: your-registry/finsight:staging-java25
        imagePullPolicy: Always
        ports:
        - name: http
          containerPort: 8080
          protocol: TCP
        env:
        - name: SPRING_PROFILES_ACTIVE
          valueFrom:
            configMapKeyRef:
              name: finsight-config
              key: SPRING_PROFILES_ACTIVE
        - name: DATABASE_URL
          value: "jdbc:mysql://mysql-staging:3306/finsight_staging?useSSL=true"
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: finsight-secrets
              key: DATABASE_USERNAME
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: finsight-secrets
              key: DATABASE_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: finsight-secrets
              key: JWT_SECRET
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        securityContext:
          runAsNonRoot: true
          runAsUser: 1000
          allowPrivilegeEscalation: false
          capabilities:
            drop:
            - ALL
---
apiVersion: v1
kind: Service
metadata:
  name: finsight-backend-service
  namespace: finsight-staging
spec:
  type: LoadBalancer
  selector:
    app: finsight
  ports:
  - name: http
    port: 80
    targetPort: 8080
    protocol: TCP
```

### Step 2: Deploy to Kubernetes

```bash
# Apply manifests
kubectl create namespace finsight-staging
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml

# Wait for rollout
kubectl rollout status deployment/finsight-backend -n finsight-staging --timeout=5m

# Get service endpoint
kubectl get svc finsight-backend-service -n finsight-staging
```

### Step 3: Verify K8s Deployment

```bash
# Check pods
kubectl get pods -n finsight-staging

# Check logs
kubectl logs -f deployment/finsight-backend -n finsight-staging

# Port forward for testing
kubectl port-forward svc/finsight-backend-service 8080:80 -n finsight-staging

# Test endpoint
curl http://localhost:8080/actuator/health
```

---

## Docker Compose (Staging)

For local staging tests without complex infrastructure:

```yaml
# docker-compose.staging.yml
version: '3.9'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: finsight_staging
      MYSQL_USER: finsight_user
      MYSQL_PASSWORD: finsight_pass
      MYSQL_ROOT_PASSWORD: root_pass_staging
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: .
      dockerfile: Dockerfile
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:mysql://mysql:3306/finsight_staging
      DATABASE_USERNAME: finsight_user
      DATABASE_PASSWORD: finsight_pass
      JWT_SECRET: staging-secret-key-base64
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    ports:
      - "80:80"
    depends_on:
      - backend
```

Deploy with:

```bash
docker-compose -f docker-compose.staging.yml up -d
```

---

## Post-Deployment Verification

### Immediate Checks (< 2 minutes)

```bash
# 1. Health check
curl -v http://staging-backend.example.com:8080/actuator/health
# Expected: {"status":"UP"}

# 2. Readiness check
curl -v http://staging-backend.example.com:8080/actuator/health/readiness
# Expected: {"status":"UP"}

# 3. Version info
curl http://staging-backend.example.com:8080/actuator/info
# Expected: Shows application name and version
```

### Functional Tests (5-10 minutes)

```bash
# 1. Register test user
curl -X POST http://staging-backend.example.com:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email":"test@staging.com",
    "password":"Test@1234",
    "fullName":"Staging Test User"
  }'
# Expected: 201 Created

# 2. Login
curl -X POST http://staging-backend.example.com:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@staging.com","password":"Test@1234"}'
# Expected: JWT token in response

# 3. Get user profile (with JWT)
JWT_TOKEN="<token-from-login>"
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://staging-backend.example.com:8080/api/profile
# Expected: User profile data

# 4. Create transaction
curl -X POST http://staging-backend.example.com:8080/api/transactions \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5000,
    "categoryId": 1,
    "description": "Staging test transaction",
    "date": "2026-05-02",
    "type": "EXPENSE"
  }'
# Expected: 201 Created with transaction ID
```

### Performance Verification

```bash
# Run performance test (requires ab from Apache Bench)
./scripts/performance-test.sh

# Expected output:
# - Health endpoint: > 100 req/sec
# - API endpoints: > 50 req/sec
# - Avg response: < 50ms
# - Zero failed requests
```

---

## Rollback Procedure

### If Deployment Fails

```bash
# Docker-based rollback
docker stop finsight-staging
docker rm finsight-staging

# Redeploy previous image
docker run -d \
  --name finsight-staging \
  your-registry/finsight:previous-version

# K8s rollback
kubectl rollout undo deployment/finsight-backend -n finsight-staging

# Verify rollback
kubectl rollout status deployment/finsight-backend -n finsight-staging
```

### Database Rollback

```bash
# Restore from backup (Flyway manages migrations)
# If a migration failed, revert the pom.xml change and redeploy
mysql -u finsight_user -p finsight_staging < backup_$(date -u +%Y%m%d_%H%M%S).sql
```

---

## Monitoring

### Real-Time Logs

```bash
# Docker logs
docker logs -f finsight-staging

# K8s logs
kubectl logs -f deployment/finsight-backend -n finsight-staging
```

### Metrics Dashboard

Access Spring Boot Actuator metrics:

```bash
# JVM metrics
curl http://staging-backend.example.com:8080/actuator/metrics/jvm.memory.used | jq

# GC metrics
curl http://staging-backend.example.com:8080/actuator/metrics/jvm.gc.pause | jq

# HTTP request metrics
curl http://staging-backend.example.com:8080/actuator/metrics/http.server.requests | jq
```

### Alerts to Configure

- **CPU > 80%**: Scale up or investigate
- **Memory > 90%**: Check for leaks
- **Error Rate > 1%**: Investigate application logs
- **Response Time p99 > 500ms**: Performance degradation
- **Pod Restarts > 3**: Crash loop suspected

---

## Troubleshooting

### Issue: "Connection refused" to database

```bash
# Check MySQL connectivity
mysql -h staging-mysql.internal -u finsight_user -p finsight_staging -e "SELECT 1"

# Check Docker network
docker network ls
docker network inspect finsight_default  # Check IP assignments

# For K8s
kubectl exec -it deployment/finsight-backend -n finsight-staging -- \
  mysql -h mysql-staging -u finsight_staging_user -p finsight_staging -e "SELECT 1"
```

### Issue: "Java 25 not found" during Docker build

```bash
# Verify Dockerfile uses correct image
cat Dockerfile | grep "FROM eclipse-temurin"
# Should show: eclipse-temurin:25-jdk-alpine

# Build with explicit tag
docker build --build-arg JAVA_VERSION=25 -t finsight:staging-java25 .
```

### Issue: High memory usage

```bash
# Check JVM heap size
curl http://staging-backend.example.com:8080/actuator/metrics/jvm.memory.max | jq

# Adjust heap size (in deployment config)
# Add to JAVA_OPTS: -Xms512m -Xmx1g
```

### Issue: Slow response times

```bash
# Check GC pause times
curl http://staging-backend.example.com:8080/actuator/metrics/jvm.gc.pause | jq

# Monitor database slow queries
mysql> SHOW VARIABLES LIKE 'slow_query_log';
mysql> SHOW VARIABLES LIKE 'long_query_time';

# Check Spring Boot metrics for slow endpoints
curl http://staging-backend.example.com:8080/actuator/metrics | jq '.names[]' | grep http
```

---

## FAQ

**Q: Do I need to change my code for Java 25?**  
A: No. Java 25 is backward compatible with Java 17. No code changes required.

**Q: How do I measure performance improvement?**  
A: Run `./scripts/performance-test.sh` before and after deployment, compare throughput.

**Q: Can I deploy to production after staging?**  
A: Yes, use the same Docker image. Kubernetes manifests are production-ready.

**Q: What if I need to rollback?**  
A: Keep previous Docker image tags and use `docker run` with the old image or `kubectl rollout undo`.

**Q: How is database migration handled?**  
A: Flyway automatically runs migrations on startup. Rollback requires manual SQL restore or image revert.

---

## Support

For issues during deployment:
1. Check logs: `docker logs finsight-staging` or `kubectl logs ...`
2. Verify prerequisites: Java 25, MySQL 8.0, Docker
3. Run health checks: `curl /actuator/health`
4. Check this guide's Troubleshooting section

---

**Last Updated**: May 2, 2026  
**Java Version**: 25 LTS  
**Status**: Production-Ready ✅
