# 🚀 Start Your Backend - Quick Guide

## ✅ The Fix is Applied

I've fixed the startup issue. Here's what to do:

## Step 1: Clean the Project (Important!)

In IntelliJ:
1. Open the **Maven** panel (usually on the right side)
2. Expand **Lifecycle**
3. Double-click **clean**
4. Wait for "BUILD SUCCESS"

OR run this command in terminal:
```bash
mvn clean
```

## Step 2: Start the Backend

In IntelliJ:
1. Find `FixitApplication.java` in:
   ```
   src/main/java/com/fixit/fixit/FixitApplication.java
   ```
2. Right-click on the file
3. Select **Run 'FixitApplication'**

OR click the green play button ▶️ next to the main method

## Step 3: Watch the Console

You should see:

```
  ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v4.0.4)

... (lots of startup messages) ...

Hibernate: create table categories ...
Hibernate: create table cities ...
Hibernate: create table countries ...
... (more CREATE TABLE statements) ...

Hibernate: insert into countries ...
Hibernate: insert into cities ...
... (INSERT statements from data.sql) ...

Started FixitApplication in 5.123 seconds ✅
```

## Step 4: Verify It's Running

### Quick Test 1: Check the Console
Look for this line:
```
Started FixitApplication in X.XXX seconds
```

### Quick Test 2: Open H2 Console
1. Open browser: http://localhost:8080/h2-console
2. Should see H2 login page ✅

### Quick Test 3: Test API
1. Open browser: http://localhost:8080/api/auth/countries
2. Should see JSON with countries ✅

## ✅ Success Indicators

- ✅ No red error messages in console
- ✅ "Started FixitApplication" appears
- ✅ H2 console opens
- ✅ Port 8080 is accessible

## ❌ If You See Errors

### Error: "Port 8080 already in use"

**Fix:**
```bash
# Find what's using port 8080
netstat -ano | findstr :8080

# Kill that process (replace XXXX with PID from above)
taskkill /PID XXXX /F
```

Then restart backend.

### Error: "Table not found" (again)

**Fix:**
1. Stop backend
2. Delete the `target` folder completely
3. In IntelliJ: Maven → clean
4. Start backend again

### Error: "Cannot find main class"

**Fix:**
1. In IntelliJ: File → Invalidate Caches
2. Select "Invalidate and Restart"
3. Wait for IntelliJ to restart
4. Try running again

## 🎯 What Changed

I added one line to fix the startup order:
```properties
spring.jpa.defer-datasource-initialization=true
```

This ensures:
1. Hibernate creates tables FIRST
2. Then data.sql inserts data
3. No more "table not found" errors!

## 📋 After Backend Starts

Once you see "Started FixitApplication":

1. **Keep backend running** in IntelliJ
2. **Open new terminal** for frontend
3. **Start frontend:**
   ```bash
   cd frontend
   npm run dev
   ```
4. **Open browser:** http://localhost:3000
5. **Test registration** - countries should now appear!

## 🆘 Still Having Issues?

Check `BACKEND_STARTUP_FIX.md` for detailed troubleshooting.

---

## Ready? Let's Go! 🚀

1. Maven → clean
2. Run FixitApplication.java
3. Wait for "Started FixitApplication"
4. You're ready to test!
