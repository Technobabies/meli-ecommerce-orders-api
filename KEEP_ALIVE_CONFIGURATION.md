# 🔄 Keep-Alive Service Configuration Guide

## 📋 Overview

The Keep-Alive Service prevents free-tier services (like Render) from going to sleep due to inactivity by having services ping each other every 4 minutes.

## 🎯 How It Works

```
Service A ──ping every 4 min──> Service B
    ↑                               │
    │                               │
    └───────ping every 4 min────────┘
```

Each service keeps the others alive by making HTTP requests to their health endpoints.

## ⚙️ Configuration

### 1. Environment Variables (Render, Heroku, etc.)

The endpoints are configured through environment variables, **NOT** hardcoded in the application.

#### In Render Dashboard:

1. Go to your service
2. Click on **"Environment"** tab
3. Add new environment variable:
   - **Key:** `KEEPALIVE_ENDPOINTS`
   - **Value:** Comma-separated list of URLs

**Example:**
```
KEEPALIVE_ENDPOINTS=https://meli-products-api.onrender.com/api/v1/ping,https://meli-users-api.onrender.com/api/v1/ping
```

#### In Heroku:

```bash
heroku config:set KEEPALIVE_ENDPOINTS="https://service1.herokuapp.com/api/v1/ping,https://service2.herokuapp.com/api/v1/ping"
```

#### In Docker Compose:

```yaml
environment:
  - KEEPALIVE_ENDPOINTS=http://service1:8080/api/v1/ping,http://service2:8080/api/v1/ping
```

### 2. How Spring Boot Reads It

In `application-prod.properties`:

```properties
keepalive.enabled=true
keepalive.endpoints=${KEEPALIVE_ENDPOINTS:}
```

- `${KEEPALIVE_ENDPOINTS:}` → Reads from environment variable
- `:}` → If not set, defaults to empty list (no pings)
- Spring Boot automatically converts comma-separated string to `List<String>`

### 3. Application Properties

#### Development (`application-dev.properties`):
```properties
keepalive.enabled=false
keepalive.endpoints=
```
- Keep-alive is **disabled** in development
- No automatic pings

#### Production (`application-prod.properties`):
```properties
keepalive.enabled=true
keepalive.endpoints=${KEEPALIVE_ENDPOINTS:}
```
- Keep-alive is **enabled** in production
- Reads endpoints from environment variable
- If `KEEPALIVE_ENDPOINTS` is not set or empty → no pings (safe default)

## 📊 Configuration Examples

### Single Service (No other backends)

**No configuration needed!**

The service will:
- ✅ Start normally
- ✅ Have keep-alive enabled but with empty endpoint list
- ✅ Not make any pings (nothing to ping)
- ✅ Be kept alive by GitHub Actions instead

### Two Services

**Service A (Orders API):**
```bash
KEEPALIVE_ENDPOINTS=https://meli-products-api.onrender.com/api/v1/ping
```

**Service B (Products API):**
```bash
KEEPALIVE_ENDPOINTS=https://meli-orders-api.onrender.com/api/v1/ping
```

### Three Services

**Service A (Orders API):**
```bash
KEEPALIVE_ENDPOINTS=https://meli-products-api.onrender.com/api/v1/ping,https://meli-users-api.onrender.com/api/v1/ping
```

**Service B (Products API):**
```bash
KEEPALIVE_ENDPOINTS=https://meli-orders-api.onrender.com/api/v1/ping,https://meli-users-api.onrender.com/api/v1/ping
```

**Service C (Users API):**
```bash
KEEPALIVE_ENDPOINTS=https://meli-orders-api.onrender.com/api/v1/ping,https://meli-products-api.onrender.com/api/v1/ping
```

## 🔍 Verification

### 1. Check Service Logs

After deploying, check your service logs in Render:

```
2025-11-22T13:00:00.123  INFO --- [scheduling-1] c.m.m.s.KeepAliveService : [2025-11-22 13:00:00] Starting keep-alive ping to 2 service(s)
2025-11-22T13:00:01.456  INFO --- [scheduling-1] c.m.m.s.KeepAliveService : [2025-11-22 13:00:01] Successfully pinged: https://meli-products-api.onrender.com/api/v1/ping
2025-11-22T13:00:02.789  INFO --- [scheduling-1] c.m.m.s.KeepAliveService : [2025-11-22 13:00:02] Successfully pinged: https://meli-users-api.onrender.com/api/v1/ping
```

### 2. Test Manually

```bash
# Test the ping endpoint
curl https://your-service.onrender.com/api/v1/ping

# Expected response:
{"success":true,"message":"pong","data":"Service is alive"}
```

### 3. Check Environment Variables

In Render dashboard:
1. Go to service → Environment
2. Verify `KEEPALIVE_ENDPOINTS` is set correctly
3. No spaces around commas
4. URLs start with `https://`

## 🚨 Troubleshooting

### No logs showing keep-alive activity

**Possible causes:**

1. **`KEEPALIVE_ENDPOINTS` is not set or empty**
   - Solution: Add the environment variable in Render

2. **URLs are incorrect**
   - Solution: Verify URLs are accessible (test with curl)

3. **Services are not deployed yet**
   - Solution: Deploy all services first, then add environment variables

### Connection timeouts

```
Failed to ping https://... - Timeout or connection error
```

**Possible causes:**

1. Target service is down or sleeping
2. URL is incorrect
3. Network issues

**Solution:**
- Verify the URL manually with curl
- Check target service is running
- Wait for GitHub Actions to wake it up first

### Environment variable not working

**Common mistakes:**

❌ `KEEPALIVE_ENDPOINTS=https://service1.com/ping, https://service2.com/ping`
   (Space after comma)

✅ `KEEPALIVE_ENDPOINTS=https://service1.com/ping,https://service2.com/ping`
   (No spaces)

❌ Using `http://` instead of `https://` for Render services

✅ Always use `https://` for external services

## 📝 Step-by-Step Setup

### For New Deployment:

1. **Deploy all your services first** (without keep-alive configured)
2. **Note down each service's URL**
3. **For each service:**
   - Go to Render → Service → Environment
   - Add `KEEPALIVE_ENDPOINTS` with URLs of OTHER services
   - Save (service will auto-redeploy)
4. **Verify in logs** that pings are happening
5. **Done!** Services will keep each other alive

### Example Timeline:

```
Time    | Action
--------|--------------------------------------------------------
10:00   | Deploy Service A (orders-api)
10:05   | Deploy Service B (products-api)
10:10   | Add KEEPALIVE_ENDPOINTS to Service A pointing to B
10:12   | Service A redeploys and starts pinging B
10:15   | Add KEEPALIVE_ENDPOINTS to Service B pointing to A
10:17   | Service B redeploys and starts pinging A
10:21   | Both services are now keeping each other alive! ✅
```

## 🔗 Related Documentation

- Main README: Service overview and general setup
- GitHub Actions: `.github/workflows/keep-alive.yml` - External keep-alive (every 3 min)
- Source code: `KeepAliveService.java` - Implementation details

## 💡 Best Practices

1. **Use `/api/v1/ping` for keep-alive** (lightweight, no DB queries)
2. **Use `/api/v1/health` for monitoring** (includes DB status)
3. **Don't create circular dependencies** in application logic
4. **Monitor logs initially** to ensure it's working
5. **Update environment variables** when adding/removing services

## ❓ FAQ

**Q: Do I need to configure this if I only have one service?**
A: No. Leave `KEEPALIVE_ENDPOINTS` empty or unset. GitHub Actions will keep it alive.

**Q: What happens if I configure the wrong URL?**
A: The service logs will show warnings but will continue working normally. Fix the URL when you notice.

**Q: How often does it ping?**
A: Every 4 minutes (240,000 milliseconds). Configurable in `KeepAliveService.java`.

**Q: Does this cost money?**
A: No. The pings are lightweight HTTP requests within free tier limits.

**Q: Can I test this locally?**
A: Yes, but you need to temporarily enable it in `application-dev.properties` and set endpoints to `http://localhost:8080/api/v1/ping`.

**Q: What if a service is temporarily down?**
A: The keep-alive will log a warning and continue. It will retry in 4 minutes.
