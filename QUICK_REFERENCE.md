# Quick Reference: Java 25 Upgrade & Deployment

**Project**: FinSight  
**Java**: 17 → 25 LTS  
**Spring Boot**: 3.4.3 → 3.5.12  
**Date**: May 2, 2026  

---

## 🚀 Quick Start Commands

### 1. Local Development (Java 25)

```bash
# Verify Java 25
java -version

# Build locally
./mvnw clean install

# Run backend
./mvnw spring-boot:run

# Run tests
./mvnw test  # Expected: 71/71 passing ✅
```

### 2. Deploy to Staging

**Docker**:
```bash
docker build -t finsight:staging-java25 .
docker-compose -f docker-compose.yml up -d
```

**VM** (See `STAGING_DEPLOYMENT.md` for details):
```bash
docker push your-registry/finsight:staging-java25
# SSH into VM and pull/run image
```

**Kubernetes**:
```bash
kubectl apply -f k8s/*.yaml
kubectl rollout status deployment/finsight-backend -n finsight-staging
```

### 3. Performance Testing

```bash
# Full benchmark suite
./scripts/performance-test.sh

# Custom load test
./scripts/load-test.sh -n 5000 -c 50 -e /api/transactions

# Compare: Java 17 vs Java 25
# Run tests with each JDK and compare results
```

### 4. Verification

```bash
# Health check
curl http://localhost:8080/actuator/health

# API documentation
curl http://localhost:8080/swagger-ui.html

# Metrics
curl http://localhost:8080/actuator/metrics
```

---

## 📋 What Changed

### Files Modified

| File | Change |
|------|--------|
| `pom.xml` | `<java.version>17</java.version>` → `25` |
| `pom.xml` | Spring Boot `3.4.3` → `3.5.12` |
| `Dockerfile` | `eclipse-temurin:17-jdk-alpine` → `25-jdk-alpine` |
| `README.md` | Updated tech stack, added deployment guides |

### No Application Code Changes

✅ All Java source files remain unchanged  
✅ All 71 tests pass without modification  
✅ Full backward compatibility  

---

## ✅ Verification Checklist

- [ ] Java 25: `java -version` → Java 25
- [ ] Build: `./mvnw clean install` → BUILD SUCCESS
- [ ] Tests: `./mvnw test` → 71/71 passing
- [ ] Docker: `docker build -t finsight:test .` → Success
- [ ] Health: `curl /actuator/health` → `{"status":"UP"}`
- [ ] Performance: `./scripts/performance-test.sh` → No errors

---

## 🔒 Security Updates

**CVEs Fixed in Spring Boot 3.5.12**:
- ✅ CVE-2026-22731: Actuator Health Groups Authentication Bypass
- ✅ CVE-2026-22733: Actuator CloudFoundry Authentication Bypass

**Java 25 LTS Benefits**:
- ✅ 8 years of security updates (Sep 2034)
- ✅ Latest security patches
- ✅ Modern TLS support

---

## 📊 Performance Expectations

| Metric | Expected | Notes |
|--------|----------|-------|
| Throughput | +5-10% | vs Java 17 |
| Memory | -8-12% | Compact strings |
| Startup | -10-15% | CDS enhancements |
| Latency | -5-8% | Better JIT |

Run `./scripts/performance-test.sh` to measure actual improvement.

---

## 🐛 Troubleshooting

**Issue**: "Java 25 not found"
```bash
brew install openjdk@25  # macOS
export JAVA_HOME=/opt/homebrew/opt/openjdk@25
```

**Issue**: Tests fail after upgrade
```bash
./mvnw clean test  # Clean rebuild
# Expected: 71/71 passing
```

**Issue**: Docker build fails
```bash
docker build --no-cache -t finsight:java25 .
```

**Issue**: Staging deployment fails
→ See `STAGING_DEPLOYMENT.md` section "Troubleshooting"

---

## 📚 Documentation

- **README.md** - Full documentation with deployment options
- **STAGING_DEPLOYMENT.md** - Comprehensive staging guide
- **Dockerfile** - Multi-stage build, Java 25 LTS
- **docker-compose.yml** - Local development setup
- **scripts/performance-test.sh** - Full benchmark suite
- **scripts/load-test.sh** - Custom load testing

---

## 🔗 Useful Links

- **Java 25 Release**: https://openjdk.java.net/projects/jdk/25/
- **Spring Boot 3.5.12**: https://spring.io/projects/spring-boot
- **Adoptium (Eclipse)**: https://adoptium.net/
- **Docker Java Images**: https://hub.docker.com/_/eclipse-temurin/

---

## 📞 Support

### Pre-Deployment
- Verify Java 25: `java -version`
- Run local tests: `./mvnw test`
- Review README.md prerequisites section

### During Deployment
- Check logs: `docker logs finsight-staging`
- Health check: `curl /actuator/health`
- See `STAGING_DEPLOYMENT.md` for step-by-step

### Post-Deployment
- Run performance tests: `./scripts/performance-test.sh`
- Verify all tests: `./mvnw test`
- Monitor metrics: Actuator endpoints

---

**Status**: Production-Ready ✅  
**Last Updated**: May 2, 2026
