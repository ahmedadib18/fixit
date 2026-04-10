# FixIt Frontend Setup Guide

## Prerequisites

- Node.js 18+ and npm installed
- Backend server running on http://localhost:8080

## Installation Steps

### 1. Install Dependencies

```bash
cd frontend
npm install
```

This will install:
- React and React DOM
- React Router for navigation
- Axios for API calls
- STOMP and SockJS for WebSocket communication
- Vite and build tools

### 2. Start Development Server

```bash
npm run dev
```

The application will be available at: http://localhost:3000

### 3. Test the Application

#### Register a New User
1. Navigate to http://localhost:3000
2. Click "Register"
3. Fill in the registration form
4. Select user type (USER or HELPER)
5. Submit the form

#### Login
1. Use your registered credentials
2. You'll be redirected to the appropriate dashboard based on your role

#### Test User Flow
1. Login as USER
2. Search for helpers
3. View helper profiles
4. Start a session (requires helper to be available)

#### Test Helper Flow
1. Login as HELPER
2. Update your profile
3. Set availability status
4. View earnings

#### Test Admin Flow
1. Login as ADMIN
2. Manage users
3. Handle support tickets
4. Resolve disputes

## API Proxy Configuration

The Vite dev server is configured to proxy API requests:

```javascript
// vite.config.js
server: {
  proxy: {
    '/api': 'http://localhost:8080',
    '/ws': {
      target: 'http://localhost:8080',
      ws: true
    }
  }
}
```

This means:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- All `/api/*` requests are proxied to the backend

## Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── Navbar.jsx
│   │   └── PrivateRoute.jsx
│   ├── context/
│   │   └── AuthContext.jsx
│   ├── pages/
│   │   ├── auth/
│   │   │   ├── Login.jsx
│   │   │   └── Register.jsx
│   │   ├── user/
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Profile.jsx
│   │   │   ├── SearchHelpers.jsx
│   │   │   ├── HelperDetails.jsx
│   │   │   ├── Sessions.jsx
│   │   │   ├── PaymentMethods.jsx
│   │   │   └── SubmitTicket.jsx
│   │   ├── helper/
│   │   │   ├── Dashboard.jsx
│   │   │   ├── Profile.jsx
│   │   │   └── Earnings.jsx
│   │   ├── admin/
│   │   │   ├── Dashboard.jsx
│   │   │   ├── ManageUsers.jsx
│   │   │   ├── ManageTickets.jsx
│   │   │   └── ManageDisputes.jsx
│   │   └── session/
│   │       └── VideoSession.jsx
│   ├── services/
│   │   ├── api.js
│   │   ├── authService.js
│   │   ├── userService.js
│   │   ├── helperService.js
│   │   ├── searchService.js
│   │   ├── sessionService.js
│   │   ├── billingService.js
│   │   ├── reviewService.js
│   │   ├── supportService.js
│   │   ├── disputeService.js
│   │   └── adminService.js
│   ├── App.jsx
│   ├── main.jsx
│   └── index.css
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

## Key Features Implemented

### Authentication
- JWT-based authentication
- Login and registration
- Role-based access control
- Automatic token refresh handling

### User Features
- Dashboard with recent sessions
- Profile management
- Helper search with filters
- Payment method management
- Support ticket submission

### Helper Features
- Profile and specialization management
- Availability toggle
- Earnings tracking
- Certificate uploads

### Admin Features
- User management (suspend, ban, reactivate)
- Support ticket handling
- Dispute resolution

### Session Features
- Video session interface
- WebRTC video streaming
- Real-time chat
- Session controls

## Troubleshooting

### Port Already in Use
If port 3000 is already in use, Vite will automatically try the next available port.

### API Connection Issues
- Ensure backend is running on port 8080
- Check browser console for CORS errors
- Verify proxy configuration in vite.config.js

### WebRTC Issues
- Grant camera and microphone permissions
- Use HTTPS in production (WebRTC requirement)
- Check browser compatibility

### Build Issues
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Clear Vite cache
rm -rf node_modules/.vite
```

## Production Build

```bash
npm run build
```

This creates an optimized production build in the `dist/` directory.

To preview the production build:
```bash
npm run preview
```

## Next Steps

1. Test all features with the backend
2. Implement WebSocket for real-time updates
3. Add WebRTC signaling for video sessions
4. Enhance UI/UX with additional styling
5. Add error boundaries and loading states
6. Implement comprehensive testing
7. Prepare for AWS deployment
