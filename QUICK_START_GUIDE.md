# FixIt - Quick Start Guide

## 🎯 Current Status

✅ Backend is running in IntelliJ
✅ Frontend is running on localhost
✅ API connections are properly configured

## 🔧 One-Time Setup (Do This Once)

### Step 1: Load Sample Data into Database

Since you're using H2 in-memory database, you need to populate it with country/city data.

**Option A: Automatic (Recommended)**

1. Stop your backend in IntelliJ (if running)
2. The `data.sql` file I created will automatically load when you restart
3. Start your backend again in IntelliJ
4. Data will be loaded automatically

**Option B: Manual (Via H2 Console)**

1. Keep backend running
2. Open browser: http://localhost:8080/h2-console
3. Enter connection details:
   - JDBC URL: `jdbc:h2:mem:fixitdb`
   - Username: `sa`
   - Password: (leave empty)
4. Click "Connect"
5. Copy and paste the SQL from `src/main/resources/data.sql`
6. Click "Run"

### Step 2: Verify Data Loaded

1. Open: http://localhost:8080/h2-console
2. Run this query:
   ```sql
   SELECT * FROM COUNTRIES;
   ```
3. You should see 10 countries
4. Run this query:
   ```sql
   SELECT * FROM CITIES;
   ```
5. You should see 58 cities

## 🚀 Testing the Application

### Test 1: Registration

1. Open frontend: http://localhost:3000
2. Click "Register"
3. Fill in the form:
   - **User Type:** Choose "User (Need Help)" or "Helper (Provide Help)"
   - **First Name:** Your first name
   - **Last Name:** Your last name
   - **Email:** your.email@example.com
   - **Password:** Choose a password
   - **Confirm Password:** Same password
   - **Country:** Select a country (should now show options!)
   - **City:** Select a city (should populate after selecting country)
4. Click "Register"
5. You should be redirected to your dashboard

### Test 2: Login

1. Go to: http://localhost:3000/login
2. Enter your email and password
3. Click "Login"
4. You should see your dashboard

### Test 3: Admin Access

A default admin account is created:
- **Email:** admin@fixit.com
- **Password:** admin123

1. Login with admin credentials
2. You should see the Admin Dashboard
3. Try accessing:
   - Manage Users
   - Manage Tickets
   - Manage Disputes

### Test 4: User Flow (as USER)

1. Register/Login as USER
2. Click "Find Helpers" in navigation
3. Try the search (may be empty if no helpers registered)
4. Go to "Profile" and update your information
5. Try "Get Support" to submit a ticket

### Test 5: Helper Flow (as HELPER)

1. Register/Login as HELPER
2. Go to "My Profile"
3. Update your professional headline
4. Toggle your availability status
5. Check "Earnings" page

## 🐛 Troubleshooting

### Problem: Country dropdown is empty

**Solution:** Data not loaded. Follow Step 1 above.

### Problem: "Cannot connect to backend"

**Check:**
1. Is IntelliJ showing backend is running?
2. Look for "Started FixitApplication" in IntelliJ console
3. Try opening: http://localhost:8080/h2-console
4. If it opens, backend is running

### Problem: CORS errors in browser console

**Solution:**
1. Clear browser cache (Ctrl+Shift+Delete)
2. Restart both frontend and backend
3. Make sure you're accessing http://localhost:3000 (not 5173)

### Problem: 401 Unauthorized after login

**Check:**
1. Open browser DevTools (F12)
2. Go to Application tab → Local Storage
3. Check if "token" exists
4. If not, login again
5. If still failing, check backend console for JWT errors

### Problem: Registration fails with "Email already exists"

**Solution:** Email is already registered. Try:
1. Use a different email
2. OR login with existing credentials
3. OR clear database (restart backend - H2 is in-memory)

## 📊 Monitoring

### Backend Logs (IntelliJ Console)

Watch for:
- ✅ "Started FixitApplication" - Backend is ready
- ✅ SQL statements - Database operations
- ❌ Exceptions or errors - Something went wrong

### Frontend Logs (Terminal)

Watch for:
- ✅ "Local: http://localhost:3000" - Frontend is ready
- ❌ Compilation errors - Code issues

### Browser Console (F12)

Watch for:
- ✅ Successful API calls (200 status)
- ❌ CORS errors - Configuration issue
- ❌ 401 errors - Authentication issue
- ❌ 404 errors - Endpoint not found

## 🎓 Understanding the Flow

### Registration Flow
```
Frontend (Register Form)
    ↓
POST /api/auth/register
    ↓
Backend (AuthController)
    ↓
AuthenticationService
    ↓
Save to Database
    ↓
Generate JWT Token
    ↓
Return to Frontend
    ↓
Store in localStorage
    ↓
Redirect to Dashboard
```

### Login Flow
```
Frontend (Login Form)
    ↓
POST /api/auth/login
    ↓
Backend (AuthController)
    ↓
Verify credentials
    ↓
Generate JWT Token
    ↓
Return to Frontend
    ↓
Store in localStorage
    ↓
Redirect to Dashboard
```

### API Call Flow
```
Frontend (Any Page)
    ↓
Service Layer (e.g., userService)
    ↓
Axios Interceptor (adds JWT token)
    ↓
Vite Proxy (/api → :8080)
    ↓
Backend Controller
    ↓
JWT Filter (validates token)
    ↓
Service Layer
    ↓
Repository (Database)
    ↓
Return Response
    ↓
Frontend Updates UI
```

## 📝 Test Scenarios

### Scenario 1: User Finds Helper

1. Register as USER
2. Go to "Find Helpers"
3. (First, register another account as HELPER)
4. Search for helpers
5. Click on helper profile
6. View reviews and ratings
7. Start a session

### Scenario 2: Helper Manages Profile

1. Register as HELPER
2. Go to "My Profile"
3. Add professional headline
4. Add languages spoken
5. Toggle availability
6. Check earnings page

### Scenario 3: Admin Moderates

1. Login as admin (admin@fixit.com / admin123)
2. Go to "Manage Users"
3. View all registered users
4. Try suspending a user
5. Go to "Manage Tickets"
6. View support tickets

## 🔄 Next Steps After Testing

Once everything is working:

1. ✅ Test all user flows
2. ✅ Verify API connections
3. ✅ Test WebRTC video (if needed)
4. 🔄 **Migrate to PostgreSQL** (Your next task!)
5. 🔄 Deploy to AWS
6. 🔄 Configure production settings

## 📞 Quick Reference

### URLs
- **Frontend:** http://localhost:3000
- **Backend:** http://localhost:8080
- **H2 Console:** http://localhost:8080/h2-console

### Default Credentials
- **Admin:** admin@fixit.com / admin123

### Database Connection
- **URL:** jdbc:h2:mem:fixitdb
- **Username:** sa
- **Password:** (empty)

### Important Files
- **Backend Config:** `src/main/resources/application.properties`
- **Frontend Config:** `frontend/vite.config.js`
- **API Service:** `frontend/src/services/api.js`
- **Security Config:** `src/main/java/com/fixit/fixit/config/SecurityConfig.java`

## ✅ Success Checklist

- [ ] Backend running in IntelliJ
- [ ] Frontend running on localhost:3000
- [ ] H2 console accessible
- [ ] Countries and cities loaded in database
- [ ] Can register new user
- [ ] Can login successfully
- [ ] Dashboard loads after login
- [ ] Can navigate between pages
- [ ] API calls work (check Network tab)
- [ ] No CORS errors in console

If all checked, you're ready to proceed with PostgreSQL migration! 🎉
