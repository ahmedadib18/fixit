import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { userService } from '../../services/userService'
import Navbar from '../../components/Navbar'

const UserSessions = () => {
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

  const formatDuration = (startedAt, endedAt) => {
    if (!startedAt || !endedAt) return 'N/A'
    const start = new Date(startedAt)
    const end = new Date(endedAt)
    const diffMs = end - start
    const minutes = Math.floor(diffMs / 60000)
    const seconds = Math.floor((diffMs % 60000) / 1000)
    
    if (minutes > 0) {
      return `${minutes} min ${seconds} sec`
    } else {
      return `${seconds} sec`
    }
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>My Sessions</h1>
        <div className="card">
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
                  <th style={{ padding: '10px', textAlign: 'left' }}>Ended</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Duration</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sessions.map(session => (
                  <tr key={session.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '10px' }}>{session.id}</td>
                    <td style={{ padding: '10px' }}>{session.status}</td>
                    <td style={{ padding: '10px' }}>
                      {session.startedAt ? new Date(session.startedAt).toLocaleString() : 'Not started'}
                    </td>
                    <td style={{ padding: '10px' }}>
                      {session.endedAt ? new Date(session.endedAt).toLocaleString() : '-'}
                    </td>
                    <td style={{ padding: '10px' }}>
                      {formatDuration(session.startedAt, session.endedAt)}
                    </td>
                    <td style={{ padding: '10px' }}>
                      <Link to={`/session/${session.id}`} className="btn btn-primary" style={{ padding: '5px 10px' }}>
                        View
                      </Link>
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

export default UserSessions
