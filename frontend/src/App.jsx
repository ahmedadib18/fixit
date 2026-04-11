import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import PrivateRoute from './components/PrivateRoute'

// Auth Pages
import Login from './pages/auth/Login'
import Register from './pages/auth/Register'

// User Pages
import UserDashboard from './pages/user/Dashboard'
import UserProfile from './pages/user/Profile'
import SearchHelpers from './pages/user/SearchHelpers'
import HelperDetails from './pages/user/HelperDetails'
import UserSessions from './pages/user/Sessions'
import PaymentMethods from './pages/user/PaymentMethods'
import SubmitTicket from './pages/user/SubmitTicket'

// Helper Pages
import HelperDashboard from './pages/helper/Dashboard'
import HelperProfile from './pages/helper/Profile'
import HelperEarnings from './pages/helper/Earnings'
import HelperSessions from './pages/helper/Sessions'

// Session Pages
import VideoSession from './pages/session/VideoSession'

// Admin Pages
import AdminDashboard from './pages/admin/Dashboard'
import ManageUsers from './pages/admin/ManageUsers'
import ManageTickets from './pages/admin/ManageTickets'
import ManageDisputes from './pages/admin/ManageDisputes'

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* User Routes */}
          <Route path="/user/dashboard" element={<PrivateRoute role="USER"><UserDashboard /></PrivateRoute>} />
          <Route path="/user/profile" element={<PrivateRoute role="USER"><UserProfile /></PrivateRoute>} />
          <Route path="/user/search" element={<PrivateRoute role="USER"><SearchHelpers /></PrivateRoute>} />
          <Route path="/user/helper/:helperId" element={<PrivateRoute role="USER"><HelperDetails /></PrivateRoute>} />
          <Route path="/user/sessions" element={<PrivateRoute role="USER"><UserSessions /></PrivateRoute>} />
          <Route path="/user/payment-methods" element={<PrivateRoute role="USER"><PaymentMethods /></PrivateRoute>} />
          <Route path="/user/support" element={<PrivateRoute role="USER"><SubmitTicket /></PrivateRoute>} />

          {/* Helper Routes */}
          <Route path="/helper/dashboard" element={<PrivateRoute role="HELPER"><HelperDashboard /></PrivateRoute>} />
          <Route path="/helper/profile" element={<PrivateRoute role="HELPER"><HelperProfile /></PrivateRoute>} />
          <Route path="/helper/earnings" element={<PrivateRoute role="HELPER"><HelperEarnings /></PrivateRoute>} />
          <Route path="/helper/sessions" element={<PrivateRoute role="HELPER"><HelperSessions /></PrivateRoute>} />

          {/* Session Routes */}
          <Route path="/session/:sessionId" element={<PrivateRoute><VideoSession /></PrivateRoute>} />

          {/* Admin Routes */}
          <Route path="/admin/dashboard" element={<PrivateRoute role="ADMIN"><AdminDashboard /></PrivateRoute>} />
          <Route path="/admin/users" element={<PrivateRoute role="ADMIN"><ManageUsers /></PrivateRoute>} />
          <Route path="/admin/tickets" element={<PrivateRoute role="ADMIN"><ManageTickets /></PrivateRoute>} />
          <Route path="/admin/disputes" element={<PrivateRoute role="ADMIN"><ManageDisputes /></PrivateRoute>} />

          {/* Default Route */}
          <Route path="/" element={<Navigate to="/login" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  )
}

export default App
