# 🚀 MELI Orders API - Deployment Guide

## 📋 Table of Contents
- [Environment Variables](#environment-variables)
- [Render Deployment Strategy](#render-deployment-strategy)
- [Branch Strategy](#branch-strategy)
- [Supabase Database Setup](#supabase-database-setup)
- [Docker Commands](#docker-commands)
- [CI/CD with GitHub Actions](#cicd-with-github-actions)

---

## 🔐 Environment Variables

### Required for ALL environments:
```bash
SPRING_PROFILES_ACTIVE=dev|test|prod
SERVER_PORT=8080
```

### Required ONLY for Production (`prod` profile):
```bash
PROD_DB_URL=jdbc:postgresql://[host]:5432/postgres
PROD_DB_USER=postgres
PROD_DB_PASSWORD=your-password
```

### Optional:
```bash
JAVA_OPTS=-Xmx512m -Xms256m
```

### How to set these in Render:
1. Go to your service → Environment
2. Add each variable with its value
3. Click "Save Changes"
4. Service will automatically redeploy

---

## 🌐 Render Deployment Strategy

### Option 1: Single Account (Recommended) ✅

**Use ONE Render account with 3 web services:**

| Environment | Branch | Service Name | Plan | Database |
|------------|---------|--------------|------|----------|
| **Production** | `main` | meli-orders-api-prod | Free/Starter | Supabase Prod |
| **Staging** | `staging` | meli-orders-api-staging | Free | Supabase Staging |
| **Development** | Local only | N/A | Docker Compose | H2 in-memory |

**Free Tier Allocation:**
- Render free tier: 750 hours/month
- 2 services × 24/7 = ~1,440 hours needed
- **Solution:** Services sleep after 15 min inactivity (auto-wake on request)
- Both will fit in free tier if not heavily used

### Option 2: Multiple Accounts

If you need 24/7 uptime for testing:

| Account | Owner | Environment | Branch |
|---------|-------|-------------|--------|
| Account 1 | You (DevOps) | Production | `main` |
| Account 2 | Team Member 1 | Staging | `staging` |
| Account 3 | Team Member 2 | Development | `dev` |

Each account gets 750 free hours/month = 2,250 hours total

---

## 🌿 Branch Strategy

### Branch Structure:
```
main (production)
├── staging (pre-production)
    ├── feature/docker-deployment
    ├── feature/payment-cards
    ├── feature/payment-module
    └── feature/[other-features]
```

### Workflow:

1. **Create feature branch** from `staging`:
   ```bash
   git checkout staging
   git pull origin staging
   git checkout -b feature/your-feature-name
   ```

2. **Develop and commit**:
   ```bash
   git add .
   git commit -m "feat: your feature description"
   git push origin feature/your-feature-name
   ```

3. **Create Pull Request** to `staging`:
   - Go to GitHub
   - Create PR: `feature/your-feature-name` → `staging`
   - Request review
   - Merge after approval

4. **Deploy to staging** (automatic):
   - Render detects changes in `staging` branch
   - Automatically builds and deploys

5. **Promote to production**:
   ```bash
   git checkout main
   git pull origin main
   git merge staging
   git push origin main
   ```
   - Render detects changes in `main` branch
   - Automatically builds and deploys to production

### Branch Protection Rules (Recommended):

Set these in GitHub:

**For `main` branch:**
- ✅ Require pull request reviews (at least 1)
- ✅ Require status checks to pass
- ✅ Require branches to be up to date
- ✅ Do not allow force pushes
- ✅ Do not allow deletions

**For `staging` branch:**
- ✅ Require pull request reviews (at least 1)
- ✅ Require status checks to pass
- ✅ Do not allow force pushes

---

## 🗄️ Supabase Database Setup

### Strategy 1: Single Project, Multiple Schemas ✅ Recommended

**One Supabase project with 3 schemas:**

```sql
-- Connect to your Supabase database and run:

-- 1. Create schemas
CREATE SCHEMA IF NOT EXISTS production;
CREATE SCHEMA IF NOT EXISTS staging;
CREATE SCHEMA IF NOT EXISTS development;

-- 2. Create tables in each schema
-- Production schema
CREATE TABLE production.orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_by UUID NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Repeat for staging and development schemas
-- (same structure for all)
```

**Connection strings:**
```bash
# Production
PROD_DB_URL=jdbc:postgresql://your-project.supabase.co:5432/postgres?currentSchema=production

# Staging
PROD_DB_URL=jdbc:postgresql://your-project.supabase.co:5432/postgres?currentSchema=staging

# Development (if deployed)
PROD_DB_URL=jdbc:postgresql://your-project.supabase.co:5432/postgres?currentSchema=development
```

### Strategy 2: Separate Projects

**Use 2 free Supabase projects:**
- Project 1: Production
- Project 2: Staging + Dev (use different schemas)

---

## 🐳 Docker Commands

### Local Development:

```bash
# Build image
docker build -t meli-orders-api .

# Run with dev profile (H2 database)
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  meli-orders-api

# Run with docker-compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop
docker-compose down
```

### Production-like testing locally:

```bash
# Create .env file with your Supabase credentials
cat > .env << 'EOF'
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
PROD_DB_URL=jdbc:postgresql://your-project.supabase.co:5432/postgres
PROD_DB_USER=postgres
PROD_DB_PASSWORD=your-password
JAVA_OPTS=-Xmx512m -Xms256m
EOF

# Run with docker-compose (reads .env automatically)
docker-compose up -d
```

### Useful commands:

```bash
# Rebuild image
docker-compose build --no-cache

# View container status
docker ps

# Execute commands in running container
docker-compose exec api sh

# Remove all containers and volumes
docker-compose down -v
```

---

## 🔄 CI/CD with GitHub Actions

### Automatic Docker Build on Push

Create `.github/workflows/docker-build.yml`:

```yaml
name: Docker Build and Test

on:
  push:
    branches: [ main, staging, feature/** ]
  pull_request:
    branches: [ main, staging ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Run tests
      run: mvn clean test
    
    - name: Build with Maven
      run: mvn clean package -DskipTests
    
    - name: Build Docker image
      run: docker build -t meli-orders-api .
    
    - name: Test Docker image
      run: |
        docker run -d -p 8080:8080 \
          -e SPRING_PROFILES_ACTIVE=dev \
          --name test-container \
          meli-orders-api
        sleep 30
        curl -f http://localhost:8080/api/v1/ || exit 1
        docker stop test-container
```

---

## 📊 Monitoring and Health Checks

### Health Check Endpoint:
```
GET /api/v1/
```

Returns:
```json
{
  "message": "Welcome to Meli Order Management Service",
  "status": "UP",
  "timestamp": "2025-11-20T10:00:00"
}
```

### Render Monitoring:
- Render provides basic monitoring in dashboard
- Check logs: Service → Logs
- Check metrics: Service → Metrics

### Custom monitoring (optional):
- Add Spring Boot Actuator for detailed health checks
- Integrate with UptimeRobot (free tier: 50 monitors)

---

## 🆘 Troubleshooting

### Issue: Container fails to start

**Check logs:**
```bash
docker-compose logs api
```

**Common causes:**
- Missing environment variables
- Wrong database credentials
- Port already in use

### Issue: Database connection fails

**Verify connection:**
```bash
# From container
docker-compose exec api sh
wget -q -O - http://localhost:8080/api/v1/
```

**Check database URL format:**
```
jdbc:postgresql://host:5432/database?currentSchema=schema_name
```

### Issue: Render service won't start

1. Check environment variables in Render dashboard
2. View build logs in Render
3. Ensure Dockerfile is in repository root
4. Check that branch name matches render.yaml

---

## 📝 Deployment Checklist

### Before deploying to production:

- [ ] All tests passing locally
- [ ] Docker image builds successfully
- [ ] Application runs in Docker locally
- [ ] Database migrations applied
- [ ] Environment variables documented
- [ ] Secrets configured in Render dashboard
- [ ] Health check endpoint working
- [ ] Logging configured
- [ ] Branch protection rules enabled
- [ ] Team has access to Render project

### After deployment:

- [ ] Health check returns 200 OK
- [ ] Can create an order via API
- [ ] Can retrieve orders via API
- [ ] Database contains expected data
- [ ] Logs show no errors
- [ ] Response times acceptable (<2s)

---

## 🎯 Quick Start Commands

```bash
# 1. Clone and navigate
git clone <repo-url>
cd meli-ecommerce-orders-api

# 2. Create feature branch
git checkout -b feature/your-feature staging

# 3. Test locally with Docker
docker-compose up -d

# 4. Make changes, commit, push
git add .
git commit -m "feat: description"
git push origin feature/your-feature

# 5. Create PR to staging → merge → auto-deploys

# 6. When ready for prod:
git checkout main
git merge staging
git push origin main
# → Auto-deploys to production
```

---

**Last Updated:** November 20, 2025  
**Maintained by:** DevOps Team (Jared)

