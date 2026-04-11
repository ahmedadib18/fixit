# FixIt PostgreSQL Setup Instructions

## Current Status
✅ PostgreSQL 17.9 installed
✅ PostgreSQL service running
✅ Ready to create database

## Step-by-Step Instructions

### Step 1: Create Database

**Option A: Using the batch script (Easiest)**
1. Double-click `create-database.bat`
2. Enter your postgres password when prompted
3. The script will create the database and verify it

**Option B: Manual command line**
1. Open Command Prompt or PowerShell
2. Run these commands:

```bash
# Add PostgreSQL to PATH (for this session)
set PATH=%PATH%;C:\Program Files\PostgreSQL\17\bin

# Create database
psql -U postgres -c "CREATE DATABASE fixitdb;"

# Verify database created
psql -U postgres -c "\l" | findstr fixitdb

# Test connection to new database
psql -U postgres -d fixitdb -c "SELECT current_database();"
```

**Option C: Using pgAdmin 4**
1. Open pgAdmin 4 (search in Windows Start menu)
2. Enter your postgres password
3. Right-click "Databases" → "Create" → "Database"
4. Name: `fixitdb`
5. Owner: `postgres`
6. Click "Save"

### Step 2: Set Environment Variables

You need to set these variables so Spring Boot can connect to PostgreSQL:
- `DB_USERNAME=postgres`
- `DB_PASSWORD=your_postgres_password`

**Option A: Set Permanently (Recommended)**
1. Press `Win + R`
2. Type `sysdm.cpl` and press Enter
3. Go to "Advanced" tab
4. Click "Environment Variables"
5. Under "User variables", click "New"
6. Add first variable:
   - Variable name: `DB_USERNAME`
   - Variable value: `postgres`
   - Click OK
7. Click "New" again
8. Add second variable:
   - Variable name: `DB_PASSWORD`
   - Variable value: `your_postgres_password` (the password you set during installation)
   - Click OK
9. Click OK on all dialogs
10. **IMPORTANT**: Close and reopen any terminals/IDEs for changes to take effect

**Option B: Set for Current Session Only**

In PowerShell:
```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_postgres_password"
```

In Command Prompt:
```cmd
set DB_USERNAME=postgres
set DB_PASSWORD=your_postgres_password
```

**Verify Environment Variables:**
```powershell
# PowerShell
echo $env:DB_USERNAME
echo $env:DB_PASSWORD

# Command Prompt
echo %DB_USERNAME%
echo %DB_PASSWORD%
```

### Step 3: Switch to Production Profile

Edit `src/main/resources/application.properties`:

Find this line:
```properties
spring.profiles.active=dev
```

Change it to:
```properties
spring.profiles.active=prod
```

### Step 4: Build and Run Application

```bash
# Clean build
mvn clean install

# Run application
mvn spring-boot:run
```

**Watch for these success indicators:**
```
✅ HikariPool-1 - Starting...
✅ Hibernate: create table users...
✅ Hibernate: create table helper_profiles...
✅ Admin user created/recreated!
✅ Started FixitApplication in X seconds
```

### Step 5: Verify Migration Success

**Check Database Tables:**
```bash
# Connect to database
psql -U postgres -d fixitdb

# List all tables
\dt

# Check admin user
SELECT email, first_name, last_name, user_type FROM users;

# Check countries
SELECT COUNT(*) FROM countries;

# Check cities
SELECT COUNT(*) FROM cities;

# Check categories
SELECT COUNT(*) FROM categories;

# Exit
\q
```

**Expected Results:**
- Admin user: admin@fixit.com
- Countries: 10
- Cities: 58
- Categories: 10

**Test Application:**
1. Open browser: http://localhost:5173
2. Login as admin: admin@fixit.com / admin123
3. Register a new user
4. Create helper profile
5. **CRITICAL TEST**: Stop backend (Ctrl+C), restart it, login again
   - If data persists, migration successful! ✅

## Troubleshooting

### Issue: "psql: command not found"
**Solution:** Add PostgreSQL to PATH
```bash
set PATH=%PATH%;C:\Program Files\PostgreSQL\17\bin
```

### Issue: "password authentication failed"
**Solution:** 
1. Check your password is correct
2. Try connecting manually: `psql -U postgres`
3. If forgotten, reset password via pgAdmin 4

### Issue: "database fixitdb does not exist"
**Solution:** Create it:
```bash
psql -U postgres -c "CREATE DATABASE fixitdb;"
```

### Issue: "Connection refused to localhost:5432"
**Solution:** Start PostgreSQL service
1. Open Services (Win + R → services.msc)
2. Find "postgresql-x64-17"
3. Right-click → Start

### Issue: Application can't connect to database
**Solution:** Check environment variables
```bash
# Verify they're set
echo $env:DB_USERNAME
echo $env:DB_PASSWORD

# If not set, set them and restart terminal/IDE
```

### Issue: Tables not created
**Solution:** Check logs for errors
- Look for Hibernate errors in console
- Verify `spring.jpa.hibernate.ddl-auto=update` in application.properties
- Check database connection successful

## Quick Reference

### Default Credentials
- **Database User**: postgres
- **Database Name**: fixitdb
- **Admin User**: admin@fixit.com / admin123

### Important URLs
- **Backend**: http://localhost:8080
- **Frontend**: http://localhost:5173
- **pgAdmin**: Search in Windows Start menu

### Key Commands
```bash
# Connect to database
psql -U postgres -d fixitdb

# List databases
\l

# List tables
\dt

# Exit psql
\q

# Start backend
mvn spring-boot:run

# Start frontend
cd frontend && npm run dev
```

## Rollback to H2

If you need to go back to H2:

1. Edit `src/main/resources/application.properties`
2. Change: `spring.profiles.active=prod` to `spring.profiles.active=dev`
3. Restart application

## Success Checklist

- [ ] PostgreSQL 17 installed
- [ ] PostgreSQL service running
- [ ] Database `fixitdb` created
- [ ] Environment variables set (DB_USERNAME, DB_PASSWORD)
- [ ] application.properties updated (spring.profiles.active=prod)
- [ ] Application builds successfully (mvn clean install)
- [ ] Application starts without errors
- [ ] Admin user can login
- [ ] Can register new users
- [ ] Data persists after restart

## Next Steps After Migration

Once migration is complete, you can:
1. Test all user flows (see PRE_MIGRATION_TEST_CHECKLIST.md)
2. Add optional features (Stripe, Email, OAuth)
3. Deploy to production
4. Set up automated backups

## Need Help?

If you encounter issues:
1. Check the Troubleshooting section above
2. Review POSTGRESQL_MIGRATION_GUIDE.md
3. Check application logs for errors
4. Verify PostgreSQL service is running
5. Test database connection manually with psql
