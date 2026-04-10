import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { userService } from '../../services/userService'
import Navbar from '../../components/Navbar'

const UserDashboard = () => {
  const { user } = useAuth()
  const [sessions, setSessions] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadSessions()
  }, [])

  const loadSessions = async () => {
    try {
      const data = await userService.getSessions(user.id)
      setSessions(data)
    } catch (err) {
      console.error('Failed to load sessions', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Welcome, {user.firstName}!</h1>
        
        <div className="card">
          <h2>Quick Actions</h2>
          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <Link to="/user/search" className="btn btn-primary">Find a Helper</Link>
            <Link to="/user/sessions" className="btn btn-secondary">View All Sessions</Link>
            <Link to="/user/support" className="btn btn-secondary">Get Support</Link>
          </div>
        </div>

        <div className="card">
          <h2>Recent Sessions</h2>
          {loading ? (
            <div className="loading">Loading sessions...</div>
          ) : sessions.length === 0 ? (
            <p>No sessions yet. <Link to="/user/search">Find a helper</Link> to get started!</p>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Session ID</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Status</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Started</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sessions.slice(0, 5).map(session => (
                  <tr key={session.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '10px' }}>{session.id}</td>
                    <td style={{ padding: '10px' }}>{session.status}</td>
                    <td style={{ padding: '10px' }}>
                      {session.startedAt ? new Date(session.startedAt).toLocaleString() : 'Not started'}
                    </td>
                    <td style={{ padding: '10px' }}>
                      {session.status === 'ENDED' && (
                        <Link to={`/session/${session.id}`} className="btn btn-primary" style={{ padding: '5px 10px' }}>
                          View Details
                        </Link>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  )
}

export default UserDashboard
