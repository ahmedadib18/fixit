# PostgreSQL Migration Guide

## Current Status
- ✅ PostgreSQL driver already in `pom.xml`
- ✅ Production profile configured (`application-prod.properties`)
- ✅ H2 running in PostgreSQL compatibility mode
- ✅ Test data available in `data.sql`
- ✅ Admin user auto-created via `DataInitializer`

## Pre-Migration Checklist

### 1. ⚠️ Backup H2 Data (If Needed)

Your current H2 database is in-memory (`jdbc:h2:mem:fixitdb`), so data is lost on restart. However, you have:
- `data.sql` with countries, cities, categories, and admin user
- `DataInitializer.java` that creates admin user programmatically

**Action**: If you've created additional test data (users, sessions, etc.), export it now:

```bash
# Start your application with H2
# Access H2 console at: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:fixitdb
# Username: sa
# Password: (leave empty)

# Export tables you want to keep:
# In H2 console, run:
SCRIPT TO 'backup.sql';
```

### 2. ⚠️ Install PostgreSQL

**Windows Installation**:
1. Download PostgreSQL from: https://www.postgresql.org/download/windows/
2. Run the installer (recommended version: 15 or 16)
3. During installation:
   - Set password for `postgres` user (remember this!)
   - Default port: 5432
   - Install pgAdmin 4 (GUI tool)

**Verify Installation**:
```bash
psql --version
```

### 3. ⚠️ Create PostgreSQL Database

**Option A: Using pgAdmin 4**
1. Open pgAdmin 4
2. Connect to PostgreSQL server
3. Right-click "Databases" → "Create" → "Database"
4. Name: `fixitdb`
5. Owner: `postgres`
6. Click "Save"

**Option B: Using Command Line**
```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE fixitdb;

# Verify
\l

# Exit
\q
```

### 4. Configure Environment Variables

Create a `.env` file in your project root (or set system environment variables):

```properties
DB_USERNAME=postgres
DB_PASSWORD=your_postgres_password
```

**For Windows System Variables**:
1. Search "Environment Variables" in Windows
2. Click "Environment Variables"
3. Under "User variables", click "New"
4. Add:
   - Variable: `DB_USERNAME`, Value: `postgres`
   - Variable: `DB_PASSWORD`, Value: `your_password`

## Migration Steps

### Step 1: Test Current H2 Setup

```bash
# Make sure everything works with H2 first
mvn clean install
mvn spring-boot:run
```

Test these flows:
- [ ] Register new user
- [ ] Login (admin@fixit.com / admin123)
- [ ] Create helper profile
- [ ] Search helpers
- [ ] Create session
- [ ] Submit review
- [ ] Admin functions

### Step 2: Switch to PostgreSQL

**Update `application.properties`**:
```properties
# Change from 'dev' to 'prod'
spring.profiles.active=prod
```

### Step 3: Start Application with PostgreSQL

```bash
# Clean build
mvn clean install

# Run with production profile
mvn spring-boot:run
```

**What happens on first run**:
1. Hibernate creates all tables (`spring.jpa.hibernate.ddl-auto=update`)
2. `data.sql` populates countries, cities, categories
3. `DataInitializer` creates admin user

### Step 4: Verify Migration

**Check Database**:
```bash
# Connect to PostgreSQL
psql -U postgres -d fixitdb

# List tables
\dt

# Check admin user
SELECT * FROM users WHERE email = 'admin@fixit.com';

# Check countries
SELECT COUNT(*) FROM countries;

# Exit
\q
```

**Test Application**:
- [ ] Login as admin (admin@fixit.com / admin123)
- [ ] Register new user
- [ ] All CRUD operations work
- [ ] Data persists after restart

### Step 5: Verify Data Persistence

```bash
# Stop the application (Ctrl+C)
# Restart it
mvn spring-boot:run

# Login again - your data should still be there!
```

## Troubleshooting

### Issue: "Connection refused to localhost:5432"
**Solution**: PostgreSQL service not running
```bash
# Windows: Check Services
# Search "Services" → Find "postgresql-x64-15" → Start
```

### Issue: "password authentication failed"
**Solution**: Check environment variables
```bash
# Verify variables are set
echo $DB_USERNAME
echo $DB_PASSWORD

# Or check in application-prod.properties
```

### Issue: "database fixitdb does not exist"
**Solution**: Create the database
```bash
psql -U postgres -c "CREATE DATABASE fixitdb;"
```

### Issue: Tables not created
**Solution**: Check Hibernate settings
- Verify `spring.jpa.hibernate.ddl-auto=update` in `application.properties`
- Check logs for errors

### Issue: data.sql not running
**Solution**: Check initialization setting
- Verify `spring.jpa.defer-datasource-initialization=true` is set
- Check for SQL syntax errors in logs

## Rollback to H2

If you need to go back to H2:

```properties
# In application.properties
spring.profiles.active=dev
```

## Post-Migration (Optional)

### Add Stripe Integration
- Get API keys from https://stripe.com/
- Add to environment variables:
  ```
  STRIPE_SECRET_KEY=sk_test_...
  STRIPE_PUBLISHABLE_KEY=pk_test_...
  ```

### Add Email Service
- Configure SMTP settings in `application-prod.properties`:
  ```properties
  spring.mail.host=smtp.gmail.com
  spring.mail.port=587
  spring.mail.username=${EMAIL_USERNAME}
  spring.mail.password=${EMAIL_PASSWORD}
  ```

### Add Google OAuth
- Get credentials from Google Cloud Console
- Update in `application.properties`:
  ```properties
  spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
  spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
  ```

## Quick Reference

### Default Credentials
- **Admin**: admin@fixit.com / admin123
- **Database**: postgres / (your password)

### Important URLs
- **Backend**: http://localhost:8080
- **Frontend**: http://localhost:5173
- **H2 Console**: http://localhost:8080/h2-console (dev only)
- **pgAdmin**: http://localhost:5050 (if installed)

### Key Files
- `application.properties` - Main config
- `application-dev.properties` - H2 config
- `application-prod.properties` - PostgreSQL config
- `data.sql` - Initial data
- `DataInitializer.java` - Admin user creation

## Summary

Your application is well-prepared for PostgreSQL migration:
1. Dependencies already configured
2. Profile-based configuration ready
3. Test data scripts available
4. Just need to install PostgreSQL and switch profile

The migration should be smooth since H2 is already in PostgreSQL compatibility mode!
