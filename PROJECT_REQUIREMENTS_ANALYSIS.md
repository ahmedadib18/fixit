# FixIt Project - Complete Requirements Analysis

## Executive Summary
FixIt (formerly ICanFixIt) is a platform connecting users with helpers for real-time video assistance. This document consolidates all strict requirements from the fixitcontext directory.

---

## 1. DATABASE SCHEMA REQUIREMENTS (schema.sql)

### Core Tables & Relationships

#### 1.1 Users & Authentication
- **users** table with fields:
  - Email (unique), password_hash, first_name, last_name, phone
  - user_type: 'USER', 'HELPER', 'ADMIN'
  - account_status: 'ACTIVE', 'SUSPENDED', 'BANNED'
  - OAuth support: google_id, oauth_provider
  - Location: city_id (FK to cities)
  - profile_image_url, created_at, updated_at, last_login

#### 1.2 Geographic Data (CountryStateCity API)
- **countries**: id, name, iso2, iso3, phone_code
- **cities**: id, name, country_id, state_name

#### 1.3 Helper Profiles
- **helpers**: user_id (unique FK), professional_headline, languages_spoken, is_available
- **categories**: name (unique), is_active
- **helper_categories**: helper_id, category_id, hourly_rate, fixed_rate, years_experience, certificate_url
- **helper_availabilities**: helper_id, day_of_week (0-6), start_time, end_time, timezone, is_active

#### 1.4 Sessions & Communication
- **sessions**: 
  - id (VARCHAR 36 - UUID)
  - user_id, helper_id, category_id
  - status: 'INITIATED', 'CONNECTED', 'IN_PROGRESS', 'PAUSED', 'ENDED', 'CANCELLED'
  - started_at, ended_at, helper_rate
  - Consent fields: user_consent_public, helper_consent_public
  - retention_months (default 12)
  
- **session_chat_messages**:
  - session_id, sender_id, message_text
  - message_type: 'TEXT', 'FILE', 'SYSTEM'
  - file_url, sent_at

#### 1.5 Payments & Billing
- **payment_methods**: user_id, stripe_payment_method_id, card_last_four, card_brand, is_default
- **transactions**:
  - session_id, payment_method_id, stripe_payment_intent_id
  - amount, platform_fee, currency (default 'USD')
  - status: 'PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED'
  - processed_at, refunded_at, refund_reason
  
- **receipts**: transaction_id, user_id, helper_id, receipt_number (unique), receipt_data, generated_at

#### 1.6 Reviews & Ratings
- **reviews**: session_id, user_id, helper_id, rating (1-5), review_text, is_public, created_at

#### 1.7 Support & Disputes
- **support_tickets**:
  - user_id, session_id (optional), subject, description
  - status: 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'
  - assigned_admin_id, created_at, updated_at, resolved_at
  
- **support_ticket_responses**: ticket_id, responder_id, response_text, created_at

- **disputes**:
  - session_id, complainant_id, respondent_id
  - dispute_type: 'BILLING', 'SERVICE'
  - amount, description
  - status: 'OPEN', 'UNDER_REVIEW', 'RESOLVED'
  - resolution, refund_amount, assigned_admin_id
  - created_at, resolved_at

---

## 2. USE CASE REQUIREMENTS (Use Case Diagrams v2)

### 2.1 Identity & Profiles (Blue)
- **UC1: Register & Authenticate** (FR 1.1, 1.2)
  - Actors: User, Helper, Admin
  - Extends to: Stripe API integration
  
- **UC2: Manage Helper Profile** (FR 1.3)
  - Actor: Helper
  - Extends to: Session Logs
  
- **UC3: Manage User Profile** (FR 1.4, 5.1)
  - Actor: User
  - Extends to: Session Logs, Stripe API

### 2.2 Session & Communication (Green)
- **UC4: Search & Filter Helpers** (FR 2.1, 2.2, 2.3, 6.2)
  - Actor: User
  - Extended by: UC5 (Select Helper)
  
- **UC5: Manage Video Session** (FR 3.1, 3.2, 3.3)
  - Actors: User, Helper
  - Extends: UC4 (Select Helper)
  - Extended by: UC7 (Session Ends)

### 2.3 Billing & Payments (Yellow)
- **UC7: Process Automatic Billing** (FR 4.1, 5.2, 5.3)
  - Extends: UC5 (Session Ends)
  - Extended by: UC8 (Billing Completed)
  - Uses: Stripe API
  
- **UC8: Rate & Review Helper** (FR 6.1)
  - Actor: User
  - Extends: UC7 (Billing Completed)
  
- **UC13: View Earnings** (FR 5.2)
  - Actor: Helper

### 2.4 Logs (Purple - Cross-cutting)
- **UC_Logs: Maintain Session Logs** (FR 4.2, 4.3, 6.3, 6.4)
  - Extends to: UC3 (User Profile), UC2 (Helper Profile)
  - Extends to: UC7 (Billing Records), UC10 (Account Activity)
  - Included by: UC11 (Review Logs)

### 2.5 Support & Administration (Red)
- **UC9: Manage Support Tickets** (FR 7.3)
  - Actor: Administrator
  
- **UC10: Moderate User Accounts** (FR 7.1)
  - Actor: Administrator
  
- **UC11: Resolve Session Disputes** (FR 7.2)
  - Actor: Administrator
  - Includes: Session Logs
  
- **UC14: Submit Support Ticket** (FR 7.3)
  - Actors: User, Helper

---

## 3. NAVIGATION ARCHITECTURE (menu-navigation-by-role-v2)

### 3.1 USER (Client) Journey
**Primary Flow**: Search → Connect → Video Session → Billing → Review

**Main Flow**:
1. WF04: Search Helpers (Navbar) → Connect
2. WF05: Video Session (Sub-screen) → End Call
3. WF06: Billing (Navbar) → Review
4. WF07: Review & Rating (Sub-screen)

**Secondary Flows**:
- WF03: User Profile (Navbar) → Session History → WF08: Session Logs
- WF06: Billing → View Log → WF08: Session Logs
- WF08: Session Logs - User gives own consent
- WF12: Submit Ticket (Navbar)

### 3.2 HELPER (Expert) Journey
**Primary Flow**: Receive Request → Accept → Video Session → Receipt

**Main Flow**:
1. Notification Modal (real-time push, WF05) → Accept
2. WF05: Video Session (uses SD05b) → End Call
3. WF06: Receipt View (helper perspective)

**Secondary Flows**:
- WF02: Helper Profile (Navbar)
- WF08: Session Logs (Navbar) - Helper gives own consent
- WF13: My Earnings (Navbar)
- WF12: Submit Ticket (Navbar)

### 3.3 ADMIN Journey
**Primary Flows**:
- WF09: Manage Support Tickets (Navbar)
- WF10: Moderate User Accounts (Navbar)
- WF11: Resolve Session Disputes (Navbar)

---

## 4. CLASS DIAGRAM ARCHITECTURE (class-diagram-v2)

### 4.1 Controllers (Green - Web Layer)
1. **AuthController**: Registration, login, Google OAuth, city lookup
2. **UserController**: Profile management, payment methods
3. **HelperController**: Profile editing, certificate upload, category management
4. **SearchController**: Helper search/filter, helper profile view, session initiation
5. **SessionController**: Session lifecycle, chat messages, consent management, log export
6. **BillingController**: Transactions, receipts, disputes, earnings
7. **ReviewController**: Review submission
8. **AdminController**: User moderation
9. **SupportTicketController**: Ticket management, responses
10. **DisputeController**: Dispute resolution

### 4.2 Services (Blue - Business Logic Layer)
1. **AuthenticationService**: Registration, login, OAuth
2. **UserService**: Profile management, session history
3. **HelperService**: Profile management, category sync, certificates
4. **SearchService**: Helper search with filters
5. **SessionService**: Session lifecycle management
6. **BillingService**: Transactions, receipts, earnings
7. **PaymentService**: Payment method management
8. **ReviewService**: Review submission and retrieval
9. **SessionLogService**: Log management, consent, export, deletion
10. **AdminService**: User moderation
11. **SupportTicketService**: Ticket CRUD, responses
12. **DisputeService**: Dispute management
13. **NotificationService**: Real-time push notifications

### 4.3 Repositories (Yellow - Data Access Layer)
- UserRepository, HelperRepository, CategoryRepository
- HelperCategoryRepository, PaymentMethodRepository
- SessionRepository, SessionChatMessageRepository
- TransactionRepository, ReceiptRepository
- ReviewRepository, SupportTicketRepository
- SupportTicketResponseRepository, DisputeRepository
- CountryRepository, CityRepository

### 4.4 Entities (Red - Domain Models)
All entities map to database tables defined in schema.sql

### 4.5 Enums (Purple)
- UserType, AccountStatus, SessionStatus
- MessageType, TransactionStatus, DisputeType
- DisputeStatus, TicketStatus

---

## 5. CRITICAL FUNCTIONAL REQUIREMENTS

### FR 1: Identity & Profiles
- FR 1.1: User registration with email verification
- FR 1.2: Authentication (email/password + Google OAuth)
- FR 1.3: Helper profile management (skills, rates, availability, certificates)
- FR 1.4: User profile management

### FR 2: Search & Discovery
- FR 2.1: Search helpers by category
- FR 2.2: Filter by rating, price, language, availability
- FR 2.3: View helper profiles with reviews
- FR 6.2: Helper search integration

### FR 3: Video Communication
- FR 3.1: Initiate video session
- FR 3.2: Real-time video/audio (Agora.io)
- FR 3.3: In-session chat with file sharing

### FR 4: Session Management
- FR 4.1: Automatic billing on session end
- FR 4.2: Session logging (video, chat, metadata)
- FR 4.3: Consent management (user & helper)

### FR 5: Billing & Payments
- FR 5.1: Payment method management (Stripe)
- FR 5.2: Automatic payment processing
- FR 5.3: Receipt generation
- FR 5.2: Helper earnings tracking

### FR 6: Reviews & History
- FR 6.1: Rate and review helpers
- FR 6.2: Review display in search
- FR 6.3: Session history for users
- FR 6.4: Session history for helpers

### FR 7: Administration
- FR 7.1: User account moderation (suspend/ban/reactivate)
- FR 7.2: Dispute resolution with refunds
- FR 7.3: Support ticket system

---

## 6. SEQUENCE DIAGRAM WORKFLOWS

### Available Sequence Diagrams:
1. **SD01**: User Registration & Authentication
2. **SD01b**: Registration - Email Exists
3. **SD02**: Helper Profile Management
4. **SD03**: User Profile & Payment
5. **SD04**: Helper Search & Discovery
6. **SD05**: Video Communication
7. **SD05b**: Helper Accepts Session
8. **SD06**: Billing & Payment
9. **SD07**: Review & Rating
10. **SD08**: Session Logs (User)
11. **SD08b**: Session Logs (Helper)
12. **SD09**: Manage Support Tickets
13. **SD10**: Moderate User Accounts
14. **SD11**: Resolve Session Disputes
15. **SD12**: Submit Support Ticket
16. **SD13**: My Earnings

---

## 7. INTERFACE DESIGN WIREFRAMES

### Available Wireframes (Excalidraw):
1. **WF01**: User Registration & Authentication
2. **WF02**: Helper Profile Management
3. **WF03**: User Profile & Payment
4. **WF04**: Helper Search & Discovery
5. **WF05**: Video Communication
6. **WF06**: Billing & Payment
7. **WF07**: Review & Rating
8. **WF08**: Session Logs
9. **WF09**: Manage Support Tickets
10. **WF10**: Moderate User Accounts
11. **WF11**: Resolve Session Disputes
12. **WF12**: Submit Support Ticket
13. **WF13**: My Earnings

---

## 8. TECHNICAL STACK REQUIREMENTS

### Backend
- Spring Boot (Java)
- PostgreSQL / H2 (development)
- Spring Security
- Spring Data JPA
- WebSocket support

### Frontend
- React with TypeScript
- Vite build tool
- React Router
- Axios for API calls

### Third-Party Integrations
- **Agora.io**: Video/audio communication
- **Stripe**: Payment processing
- **CountryStateCity API**: Geographic data
- **Google OAuth**: Social authentication

### Infrastructure
- Backend: Port 8080
- Frontend: Port 5173
- CORS configuration required
- WebSocket for real-time notifications

---

## 9. KEY BUSINESS RULES

### Session Workflow
1. User searches and selects helper
2. Session initiated (status: INITIATED)
3. Helper receives real-time notification
4. Helper accepts → status: CONNECTED
5. Video call starts → status: IN_PROGRESS
6. Either party can pause → status: PAUSED
7. Session ends → status: ENDED
8. Automatic billing triggered
9. User can review helper

### Consent & Privacy
- Both user and helper must consent to log retention
- Default retention: 12 months
- Logs can be exported by participants
- Deletion requests must be honored

### Payment Flow
- Payment methods stored via Stripe
- Automatic charge on session end
- Platform fee deducted
- Receipts generated automatically
- Disputes can trigger refunds

### Helper Availability
- Helpers set weekly availability schedule
- Real-time availability flag
- Timezone support required

### Admin Moderation
- Can suspend, ban, or reactivate users
- Must provide reason for actions
- Can resolve disputes with refunds
- Manages support tickets

---

## 10. DATA INTEGRITY REQUIREMENTS

### Indexes (Performance)
- All foreign keys indexed
- Email, user_type, account_status indexed
- Session status indexed
- Transaction status indexed
- Helper availability indexed

### Constraints
- Unique constraints: email, receipt_number, category name
- Check constraints: user_type, account_status, session_status, rating (1-5)
- Foreign key constraints with proper cascading

### Audit Fields
- created_at, updated_at timestamps
- last_login tracking
- Transaction timestamps (processed_at, refunded_at)
- Ticket timestamps (created_at, updated_at, resolved_at)

---

## IMPLEMENTATION STATUS

✅ Database schema defined
✅ Backend structure (controllers, services, repositories)
✅ Frontend structure (pages, components, services)
✅ Basic authentication flow
✅ API service layer
⚠️ Video integration (Agora.io) - needs implementation
⚠️ Payment integration (Stripe) - needs implementation
⚠️ Real-time notifications (WebSocket) - needs implementation
⚠️ Email verification - needs implementation
⚠️ File upload (certificates, chat files) - needs implementation

---

## NEXT STEPS RECOMMENDATIONS

1. Implement Agora.io video integration
2. Complete Stripe payment flow
3. Set up WebSocket for real-time notifications
4. Implement email verification service
5. Add file upload functionality
6. Complete all sequence diagram workflows
7. Implement all wireframe designs
8. Add comprehensive error handling
9. Implement logging and monitoring
10. Write integration tests

---

*This document is based on the fixitcontext directory contents and represents the strict requirements for the FixIt platform.*
