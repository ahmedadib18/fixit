# FixIt Frontend

React-based frontend for the FixIt Remote Video Assistance System.

## Tech Stack

- React 18
- Vite (Build tool)
- React Router (Navigation)
- Axios (HTTP client)
- STOMP/WebSocket (Real-time communication)
- WebRTC (Video communication)

## Project Structure

```
frontend/
├── src/
│   ├── components/       # Reusable components
│   ├── context/          # React Context (Auth)
│   ├── pages/            # Page components
│   │   ├── auth/         # Login, Register
│   │   ├── user/         # User pages
│   │   ├── helper/       # Helper pages
│   │   ├── admin/        # Admin pages
│   │   └── session/      # Video session
│   ├── services/         # API service layer
│   ├── App.jsx           # Main app component
│   ├── main.jsx          # Entry point
│   └── index.css         # Global styles
├── index.html
├── package.json
└── vite.config.js
```

## Features

### User Features
- User registration and authentication
- Search and filter helpers
- View helper profiles and reviews
- Start video sessions
- Manage payment methods
- View session history
- Submit support tickets

### Helper Features
- Helper profile management
- Set availability status
- Manage specializations and rates
- View earnings and transaction history
- Upload certificates

### Admin Features
- Manage users (suspend, ban, reactivate)
- Handle support tickets
- Resolve disputes
- View system-wide data

### Session Features
- WebRTC video communication
- Real-time chat
- Session controls (end, cancel)
- Session logs and history

## Setup Instructions

1. Install dependencies:
```bash
cd frontend
npm install
```

2. Start development server:
```bash
npm run dev
```

The frontend will run on http://localhost:3000 and proxy API requests to http://localhost:8080

3. Build for production:
```bash
npm run build
```

## API Integration

The frontend connects to the Spring Boot backend running on port 8080. All API calls are proxied through Vite's dev server configuration.

### Authentication
- JWT tokens are stored in localStorage
- Tokens are automatically attached to API requests via Axios interceptors
- Expired tokens trigger automatic logout and redirect to login

### WebSocket Connection
WebSocket connections for real-time features (video signaling, chat) connect to `/ws` endpoint.

## Environment Variables

No environment variables required for development. The Vite proxy handles API routing.

For production, update the API base URL in `src/services/api.js`.

## User Roles

The application supports three user roles:
- **USER**: Regular users seeking help
- **HELPER**: Service providers offering assistance
- **ADMIN**: System administrators

Each role has dedicated dashboards and features.

## Browser Requirements

- Modern browser with WebRTC support (Chrome, Firefox, Safari, Edge)
- Camera and microphone access for video sessions
- JavaScript enabled
- LocalStorage enabled

## Development Notes

- The app uses React Router for client-side routing
- Authentication state is managed via React Context
- All API calls go through service layer for consistency
- Private routes enforce authentication and role-based access
- Video sessions use WebRTC for peer-to-peer communication

## Next Steps

After completing the frontend:
1. Test all user flows
2. Integrate with backend API
3. Test WebRTC video functionality
4. Prepare for PostgreSQL migration
5. Deploy to AWS
