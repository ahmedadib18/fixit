import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const PrivateRoute = ({ children, role }) => {
  const { isAuthenticated, user, loading } = useAuth()

  if (loading) {
    return <div className="loading">Loading...</div>
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  if (role && user?.userType !== role) {
    return <Navigate to={`/${user?.userType.toLowerCase()}/dashboard`} replace />
  }

  return children
}

export default PrivateRoute
