# Migration Implementation Steps - Action Plan

## Current Status
- ✅ Application configured for H2 (dev profile active)
- ❌ PostgreSQL NOT installed yet
- ✅ Backend and frontend code ready
- ✅ Configuration files ready

## Phase 1: Pre-Migration Testing (H2)

### Step 1.1: Start Backend with H2
```bash
# Clean and build
mvn clean install

# Start backend
mvn spring-boot:run
```

**Expected Output**:
- Server starts on port 8080
- H2 console available at http://localhost:8080/h2-console
- Admin user created (admin@fixit.com / admin123)
- Countries, cities, categories loaded

### Step 1.2: Start Frontend
```bash
# In a new terminal
cd frontend
npm install  # If not already done
npm run dev
```

**Expected Output**:
- Frontend starts on http://localhost:5173
- Vite dev server running

### Step 1.3: Manual Testing Checklist

Open http://localhost:5173 and test:

#### Critical Path Tests (Must Pass)
1. **Authentication**
   - [ ] Register: testuser@example.com / Test123!
   - [ ] Login as admin: admin@fixit.com / admin123
   - [ ] Login as test user
   - [ ] Logout

2. **Helper Profile**
   - [ ] Login as test user
   - [ ] Create helper profile (bio, rate, categories)
   - [ ] Verify helper status

3. **Search & Discovery**
   - [ ] Search for helpers
   - [ ] View helper details
   - [ ] Check if test helper appears

4. **Basic CRUD**
   - [ ] Update user profile
   - [ ] Update helper profile
   - [ ] Verify changes persist

#### Optional Tests (Nice to Have)
- [ ] Session creation
- [ ] Payment methods
- [ ] Reviews
- [ ] Support tickets
- [ ] Admin functions

### Step 1.4: Document Issues
If any tests fail, document them here:
```
Issue 1: _______________________
Issue 2: _______________________
```

## Phase 2: Install PostgreSQL

### Step 2.1: Download PostgreSQL
1. Go to: https://www.postgresql.org/download/windows/
2. Download PostgreSQL 16 (or 15) installer
3. Run the installer

### Step 2.2: Installation Settings
During installation:
- **Password for postgres user**: Choose a strong password (REMEMBER THIS!)
- **Port**: 5432 (default)
- **Locale**: Default
- **Components**: 
  - ✅ PostgreSQL Server
  - ✅ pgAdmin 4
  - ✅ Command Line Tools
  - ❌ Stack Builder (optional)

### Step 2.3: Verify Installation
After installation, PostgreSQL should be added to PATH. Restart your terminal and run:
```bash
psql --version
```

If not found, add to PATH manually:
```
C:\Program Files\PostgreSQL\16\bin
```

### Step 2.4: Test PostgreSQL Connection
```bash
# Connect to PostgreSQL (will prompt for password)
psql -U postgres

# You should see:
# postgres=#

# Exit
\q
```

## Phase 3: Create Database

### Step 3.1: Create Database via Command Line
```bash
# Create database
psql -U postgres -c "CREATE DATABASE fixitdb;"

# Verify
psql -U postgres -c "\l" | grep fixitdb
```

### Step 3.2: OR Create via pgAdmin 4
1. Open pgAdmin 4
2. Connect to PostgreSQL server (enter password)
3. Right-click "Databases" → "Create" → "Database"
4. Name: `fixitdb`
5. Owner: `postgres`
6. Click "Save"

### Step 3.3: Verify Database Created
```bash
psql -U postgres -d fixitdb -c "SELECT current_database();"
```

Expected output: `fixitdb`

## Phase 4: Configure Environment Variables

### Step 4.1: Set Windows Environment Variables

**Option A: Via GUI**
1. Press `Win + R`, type `sysdm.cpl`, press Enter
2. Go to "Advanced" tab → "Environment Variables"
3. Under "User variables", click "New"
4. Add:
   - Variable: `DB_USERNAME`
   - Value: `postgres`
5. Click "New" again
6. Add:
   - Variable: `DB_PASSWORD`
   - Value: `your_postgres_password`
7. Click OK on all dialogs

**Option B: Via PowerShell (Current Session Only)**
```powershell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_postgres_password"
```

### Step 4.2: Verify Environment Variables
```bash
echo $env:DB_USERNAME
echo $env:DB_PASSWORD
```

**IMPORTANT**: If using PowerShell session variables, you must set them in EVERY new terminal before starting the app.

## Phase 5: Switch to PostgreSQL

### Step 5.1: Stop Current Application
If backend is running, press `Ctrl+C` to stop it.

### Step 5.2: Update Configuration
Edit `src/main/resources/application.properties`:

Change:
```properties
spring.profiles.active=dev
```

To:
```properties
spring.profiles.active=prod
```

### Step 5.3: Clean Build
```bash
mvn clean install
```

### Step 5.4: Start with PostgreSQL
```bash
mvn spring-boot:run
```

**Watch for**:
- ✅ "HikariPool-1 - Starting..." (PostgreSQL connection)
- ✅ "Hibernate: create table..." (tables being created)
- ✅ "Admin user created/recreated!" (DataInitializer)
- ✅ "Started FixitApplication in X seconds"

**If errors occur**, check:
- PostgreSQL service is running
- Environment variables are set
- Database `fixitdb` exists
- Password is correct

## Phase 6: Verify Migration

### Step 6.1: Check Database Tables
```bash
# Connect to database
psql -U postgres -d fixitdb

# List all tables
\dt

# Expected tables:
# - users
# - helper_profiles
# - sessions
# - reviews
# - countries
# - cities
# - categories
# - payment_methods
# - support_tickets
# - disputes
# - etc.

# Check admin user
SELECT email, first_name, last_name, user_type FROM users WHERE email = 'admin@fixit.com';

# Check countries loaded
SELECT COUNT(*) FROM countries;
# Expected: 10

# Check cities loaded
SELECT COUNT(*) FROM cities;
# Expected: 58

# Check categories loaded
SELECT COUNT(*) FROM categories;
# Expected: 10

# Exit
\q
```

### Step 6.2: Test Application with PostgreSQL

Open http://localhost:5173 and test:

1. **Login as Admin**
   - [ ] Email: admin@fixit.com
   - [ ] Password: admin123
   - [ ] Verify redirect to admin dashboard

2. **Register New User**
   - [ ] Create new user account
   - [ ] Login with new account
   - [ ] Verify user dashboard

3. **Create Helper Profile**
   - [ ] Fill helper profile
   - [ ] Save
   - [ ] Verify saved

4. **Test Data Persistence**
   - [ ] Stop backend (Ctrl+C)
   - [ ] Restart backend (mvn spring-boot:run)
   - [ ] Login again
   - [ ] Verify data is still there (THIS IS THE KEY TEST!)

### Step 6.3: Compare with H2 Tests
Run the same tests you did in Phase 1. Everything should work the same, but now data persists!

## Phase 7: Final Verification

### Step 7.1: Restart Test
```bash
# Stop backend
Ctrl+C

# Restart
mvn spring-boot:run

# Login and verify all data persists
```

### Step 7.2: Create Test Data
1. Register 2-3 users
2. Create helper profiles
3. Create a session
4. Add a review
5. Restart application
6. Verify all data is still there

### Step 7.3: Rollback Test (Optional)
If you want to test rollback to H2:
```properties
# Change back to:
spring.profiles.active=dev
```

Restart and verify H2 works (data will be fresh/empty).

## Success Criteria

✅ PostgreSQL installed and running
✅ Database `fixitdb` created
✅ Application starts without errors
✅ All tables created automatically
✅ Admin user exists
✅ Countries, cities, categories loaded
✅ Can register new users
✅ Can create helper profiles
✅ Data persists after restart
✅ All critical user flows work

## Troubleshooting Guide

### Error: "Connection refused to localhost:5432"
**Cause**: PostgreSQL service not running

**Solution**:
```bash
# Check service status
Get-Service -Name postgresql*

# Start service
Start-Service postgresql-x64-16
```

### Error: "password authentication failed for user postgres"
**Cause**: Wrong password or environment variables not set

**Solution**:
1. Verify environment variables: `echo $env:DB_PASSWORD`
2. Try connecting manually: `psql -U postgres`
3. If password wrong, reset it via pgAdmin

### Error: "database fixitdb does not exist"
**Cause**: Database not created

**Solution**:
```bash
psql -U postgres -c "CREATE DATABASE fixitdb;"
```

### Error: "relation users does not exist"
**Cause**: Tables not created

**Solution**:
1. Check `spring.jpa.hibernate.ddl-auto=update` in application.properties
2. Check logs for Hibernate errors
3. Verify database connection successful

### Error: "data.sql not executing"
**Cause**: Initialization timing issue

**Solution**:
1. Verify `spring.jpa.defer-datasource-initialization=true`
2. Check for SQL syntax errors in logs
3. Manually run data.sql in pgAdmin if needed

## Next Steps After Migration

### Optional Enhancements
1. **Stripe Integration** - Add payment processing
2. **Email Service** - Add email notifications
3. **Google OAuth** - Add social login
4. **Redis** - Add caching for helper availability
5. **Production Deployment** - Deploy to cloud

### Backup Strategy
```bash
# Backup database
pg_dump -U postgres fixitdb > backup_$(date +%Y%m%d).sql

# Restore database
psql -U postgres fixitdb < backup_20260411.sql
```

## Summary

This migration is straightforward because:
1. ✅ All configuration already in place
2. ✅ H2 in PostgreSQL compatibility mode
3. ✅ Hibernate handles schema creation
4. ✅ data.sql handles initial data
5. ✅ Just need to install PostgreSQL and switch profile

Total time estimate: 30-60 minutes (mostly PostgreSQL installation)
