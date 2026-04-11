import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const Navbar = () => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const getDashboardLink = () => {
    if (!user) return '/login'
    return `/${user.userType.toLowerCase()}/dashboard`
  }

  return (
    <nav className="navbar">
      <div className="navbar-content">
        <Link to={getDashboardLink()} className="navbar-brand">
          FixIt
        </Link>
        <div className="navbar-menu">
          {user && (
            <>
              <span>Welcome, {user.firstName}</span>
              {user.userType === 'USER' && (
                <>
                  <Link to="/user/search" className="navbar-link">Find Helpers</Link>
                  <Link to="/user/sessions" className="navbar-link">My Sessions</Link>
                  <Link to="/user/profile" className="navbar-link">Profile</Link>
                </>
              )}
              {user.userType === 'HELPER' && (
                <>
                  <Link to="/helper/profile" className="navbar-link">My Profile</Link>
                  <Link to="/helper/sessions" className="navbar-link">My Sessions</Link>
                  <Link to="/helper/earnings" className="navbar-link">Earnings</Link>
                </>
              )}
              {user.userType === 'ADMIN' && (
                <>
                  <Link to="/admin/users" className="navbar-link">Users</Link>
                  <Link to="/admin/tickets" className="navbar-link">Tickets</Link>
                  <Link to="/admin/disputes" className="navbar-link">Disputes</Link>
                </>
              )}
              <button onClick={handleLogout} className="btn btn-secondary">Logout</button>
            </>
          )}
        </div>
      </div>
    </nav>
  )
}

export default Navbar
