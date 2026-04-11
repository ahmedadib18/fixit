# Pre-Migration Test Checklist

Test all these flows with H2 before migrating to PostgreSQL.

## Setup
1. Ensure `spring.profiles.active=dev` in `application.properties`
2. Start backend: `mvn spring-boot:run`
3. Start frontend: `cd frontend && npm run dev`
4. Open browser: http://localhost:5173

## Test Flows

### 1. Authentication & Registration ✅

#### Register New User
- [ ] Navigate to Register page
- [ ] Fill form with valid data
- [ ] Submit registration
- [ ] Verify success message
- [ ] Check user can login

**Test Data**:
```
Email: testuser@example.com
Password: Test123!
First Name: Test
Last Name: User
```

#### Login
- [ ] Login as admin (admin@fixit.com / admin123)
- [ ] Verify redirect to admin dashboard
- [ ] Logout
- [ ] Login as regular user
- [ ] Verify redirect to user dashboard

### 2. User Profile Management ✅

#### View Profile
- [ ] Login as user
- [ ] Navigate to Profile
- [ ] Verify user details displayed

#### Update Profile
- [ ] Click Edit Profile
- [ ] Update phone number
- [ ] Update address
- [ ] Save changes
- [ ] Verify changes persisted

#### Add Payment Method
- [ ] Navigate to Payment Methods
- [ ] Add new payment method
- [ ] Verify it appears in list
- [ ] Set as default
- [ ] Delete payment method

### 3. Helper Profile Management ✅

#### Become a Helper
- [ ] Login as user
- [ ] Navigate to Helper Profile
- [ ] Fill helper profile form:
  - Bio
  - Hourly rate
  - Categories
  - Availability
- [ ] Submit profile
- [ ] Verify helper status

#### Update Helper Profile
- [ ] Edit bio
- [ ] Change hourly rate
- [ ] Update categories
- [ ] Modify availability
- [ ] Save changes

### 4. Helper Search & Discovery ✅

#### Search Helpers
- [ ] Navigate to Search Helpers
- [ ] Search without filters
- [ ] Verify helpers displayed
- [ ] Apply category filter
- [ ] Apply location filter
- [ ] Apply rating filter
- [ ] Apply price range filter

#### View Helper Details
- [ ] Click on a helper
- [ ] Verify profile details shown
- [ ] Check reviews displayed
- [ ] Check availability shown
- [ ] Verify "Request Session" button

### 5. Session Management ✅

#### Request Session (User)
- [ ] Search for helper
- [ ] Click "Request Session"
- [ ] Fill session details
- [ ] Submit request
- [ ] Verify session in "My Sessions"

#### Accept Session (Helper)
- [ ] Login as helper
- [ ] Navigate to Sessions
- [ ] View pending session
- [ ] Accept session
- [ ] Verify status changed

#### Start Video Session
- [ ] Navigate to active session
- [ ] Click "Start Video"
- [ ] Verify video interface loads
- [ ] Test chat functionality
- [ ] End session

#### View Session History
- [ ] Navigate to Sessions
- [ ] Filter by status (Completed, Cancelled)
- [ ] View session details
- [ ] Check session logs

### 6. Billing & Payments ✅

#### View Session Cost
- [ ] Complete a session
- [ ] Check billing details
- [ ] Verify hourly rate calculation
- [ ] Verify total cost

#### Process Payment
- [ ] Select payment method
- [ ] Confirm payment
- [ ] Verify payment status
- [ ] Check receipt

### 7. Reviews & Ratings ✅

#### Submit Review (User)
- [ ] Complete a session
- [ ] Navigate to "Submit Review"
- [ ] Rate helper (1-5 stars)
- [ ] Write review comment
- [ ] Submit review
- [ ] Verify review appears on helper profile

#### View Reviews (Helper)
- [ ] Login as helper
- [ ] View received reviews
- [ ] Check average rating
- [ ] Verify review details

### 8. Helper Earnings ✅

#### View Earnings Dashboard
- [ ] Login as helper
- [ ] Navigate to "My Earnings"
- [ ] Verify total earnings
- [ ] Check earnings by period
- [ ] View session breakdown

#### Earnings History
- [ ] Filter by date range
- [ ] View detailed transactions
- [ ] Check pending payments
- [ ] Verify completed payments

### 9. Support Tickets ✅

#### Submit Ticket (User)
- [ ] Navigate to Support
- [ ] Click "Submit Ticket"
- [ ] Fill ticket form:
  - Subject
  - Description
  - Priority
- [ ] Submit ticket
- [ ] Verify ticket created

#### View Tickets
- [ ] Navigate to My Tickets
- [ ] View ticket list
- [ ] Click on ticket
- [ ] View ticket details
- [ ] Add comment to ticket

### 10. Admin Functions ✅

#### Manage Users
- [ ] Login as admin
- [ ] Navigate to Manage Users
- [ ] View user list
- [ ] Search for user
- [ ] View user details
- [ ] Suspend user account
- [ ] Reactivate user account

#### Manage Support Tickets
- [ ] Navigate to Manage Tickets
- [ ] View all tickets
- [ ] Filter by status
- [ ] Assign ticket to admin
- [ ] Update ticket status
- [ ] Add admin response
- [ ] Close ticket

#### Manage Disputes
- [ ] Navigate to Manage Disputes
- [ ] View dispute list
- [ ] View dispute details
- [ ] Review evidence
- [ ] Make decision
- [ ] Update dispute status

#### View Dashboard
- [ ] Check total users count
- [ ] Check active sessions
- [ ] Check pending tickets
- [ ] View recent activity

## Data to Export (If Needed)

If you created test data you want to keep, export these tables:

```sql
-- In H2 Console (http://localhost:8080/h2-console)

-- Export users (excluding admin, will be recreated)
SELECT * FROM users WHERE email != 'admin@fixit.com';

-- Export helper profiles
SELECT * FROM helper_profiles;

-- Export sessions
SELECT * FROM sessions;

-- Export reviews
SELECT * FROM reviews;

-- Export support tickets
SELECT * FROM support_tickets;

-- Export payment methods
SELECT * FROM payment_methods;
```

## Test Results

### Issues Found
Document any issues here:
- [ ] Issue 1: _____________________
- [ ] Issue 2: _____________________
- [ ] Issue 3: _____________________

### All Tests Passed?
- [ ] Yes - Ready to migrate to PostgreSQL
- [ ] No - Fix issues before migration

## Notes

- Admin user (admin@fixit.com / admin123) will be auto-created in PostgreSQL
- Countries, cities, and categories will be loaded from data.sql
- Any other test data needs to be manually recreated or imported

## Next Steps

Once all tests pass:
1. Follow `POSTGRESQL_MIGRATION_GUIDE.md`
2. Install PostgreSQL
3. Create database
4. Switch to production profile
5. Re-run these tests with PostgreSQL
