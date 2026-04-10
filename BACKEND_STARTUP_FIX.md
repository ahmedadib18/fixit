# Backend Startup Issue - FIXED ✅

## Problem

Backend was failing to start with error:
```
Table "COUNTRIES" not found (this database is empty)
```

## Root Cause

Spring Boot was trying to run `data.sql` (insert data) BEFORE Hibernate created the tables from your entity classes.

## Solution Applied

Added this line to `application.properties`:
```properties
spring.jpa.defer-datasource-initialization=true
```

This tells Spring Boot to:
1. First: Let Hibernate create tables from your @Entity classes
2. Then: Run data.sql to insert sample data

## What Was Changed

### File: `src/main/resources/application.properties`
```properties
spring.jpa.defer-datasource-initialization=true  # ← Added this line
```

### File: `src/main/resources/application-dev.properties`
```properties
spring.datasource.url=jdbc:h2:mem:fixitdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
# Added MODE=PostgreSQL for better compatibility
```

## How to Test

1. **Stop the backend** in IntelliJ (if it's trying to run)
2. **Clean the project:**
   - In IntelliJ: Maven → Lifecycle → clean
   - Or run: `mvn clean`
3. **Start the backend again:**
   - Run `FixitApplication.java`
4. **Look for success message:**
   ```
   Started FixitApplication in X.XXX seconds
   ```

## Expected Startup Sequence

You should now see this in the console:

1. ✅ Hibernate creates tables (you'll see CREATE TABLE statements)
2. ✅ data.sql inserts sample data (you'll see INSERT statements)
3. ✅ Server starts on port 8080
4. ✅ "Started FixitApplication" message appears

## Verification Steps

After backend starts successfully:

### 1. Check H2 Console
- Open: http://localhost:8080/h2-console
- Login with:
  - JDBC URL: `jdbc:h2:mem:fixitdb`
  - Username: `sa`
  - Password: (empty)
- Run: `SELECT * FROM COUNTRIES;`
- Should see 10 countries

### 2. Test API Endpoint
- Open: http://localhost:8080/api/auth/countries
- Should return JSON array of countries

### 3. Test Frontend
- Open: http://localhost:3000
- Click "Register"
- Country dropdown should now have options!

## If Still Having Issues

### Issue: "Port 8080 already in use"

**Solution:**
1. Find what's using port 8080:
   ```bash
   netstat -ano | findstr :8080
   ```
2. Kill that process:
   ```bash
   taskkill /PID <process_id> /F
   ```
3. Restart backend

### Issue: "Cannot find data.sql"

**Solution:**
Make sure `data.sql` exists at:
```
src/main/resources/data.sql
```

### Issue: Still getting table not found

**Solution:**
1. Delete the `target` folder
2. In IntelliJ: Maven → Lifecycle → clean
3. In IntelliJ: Maven → Lifecycle → compile
4. Restart backend

## What This Means

Your backend is now configured to:
- ✅ Create tables automatically from your @Entity classes
- ✅ Load sample data from data.sql
- ✅ Work with H2 in-memory database
- ✅ Ready for PostgreSQL migration later

## Next Steps

Once backend starts successfully:
1. ✅ Test registration with country/city selection
2. ✅ Test login
3. ✅ Test all features
4. ⏭️ Proceed with PostgreSQL migration

---

## Technical Details

### Why This Happened

Spring Boot 3.x+ changed the default behavior:
- **Old behavior:** Run schema.sql → data.sql → Hibernate
- **New behavior:** Run Hibernate → data.sql (no schema.sql by default)

Since you're using Hibernate to create tables (via @Entity classes), we need to tell Spring Boot to wait for Hibernate to finish before running data.sql.

### The Fix Explained

```properties
spring.jpa.defer-datasource-initialization=true
```

This property:
- Defers data source initialization
- Lets Hibernate create tables first
- Then runs data.sql
- Ensures proper order of operations

### Alternative Approaches

If you prefer using schema.sql instead:
1. Create `schema.sql` with CREATE TABLE statements
2. Set `spring.jpa.hibernate.ddl-auto=none`
3. Spring will run schema.sql → data.sql

But current approach (using Hibernate) is better because:
- Tables match your @Entity classes exactly
- No need to maintain separate SQL files
- Easier to add new entities
- Works same way in PostgreSQL

---

## Status: ✅ FIXED

Your backend should now start successfully!
