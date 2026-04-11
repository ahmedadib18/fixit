import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { helperService } from '../../services/helperService'
import Navbar from '../../components/Navbar'

const HelperSessions = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [sessions, setSessions] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('all') // all, completed, cancelled

  useEffect(() => {
    loadSessions()
  }, [])

  const loadSessions = async () => {
    try {
      const helperData = await helperService.getHelperByUserId(user.id)
      if (helperData?.id) {
        const sessionsData = await helperService.getHelperSessions(helperData.id)
        setSessions(sessionsData)
      }
    } catch (err) {
      console.error('Failed to load sessions', err)
    } finally {
      setLoading(false)
    }
  }

  const filteredSessions = sessions.filter(session => {
    if (filter === 'all') return true
    if (filter === 'completed') return session.status === 'ENDED'
    if (filter === 'cancelled') return session.status === 'CANCELLED' || session.status === 'REJECTED'
    return true
  })

  const getStatusColor = (status) => {
    switch (status) {
      case 'ENDED': return '#28a745'
      case 'CONNECTED': return '#007bff'
      case 'INITIATED': return '#ffc107'
      case 'CANCELLED': return '#dc3545'
      case 'REJECTED': return '#6c757d'
      default: return '#6c757d'
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

  if (loading) return <><Navbar /><div className="loading">Loading...</div></>

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>My Session History</h1>

        <div className="card">
          <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
            <button 
              onClick={() => setFilter('all')} 
              className={filter === 'all' ? 'btn btn-primary' : 'btn btn-secondary'}
            >
              All ({sessions.length})
            </button>
            <button 
              onClick={() => setFilter('completed')} 
              className={filter === 'completed' ? 'btn btn-primary' : 'btn btn-secondary'}
            >
              Completed ({sessions.filter(s => s.status === 'ENDED').length})
            </button>
            <button 
              onClick={() => setFilter('cancelled')} 
              className={filter === 'cancelled' ? 'btn btn-primary' : 'btn btn-secondary'}
            >
              Cancelled ({sessions.filter(s => s.status === 'CANCELLED' || s.status === 'REJECTED').length})
            </button>
          </div>

          {filteredSessions.length === 0 ? (
            <p>No sessions found</p>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Session ID</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>User</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Category</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Status</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Date</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Duration</th>
                </tr>
              </thead>
              <tbody>
                {filteredSessions.map(session => (
                  <tr key={session.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '10px' }}>{session.id}</td>
                    <td style={{ padding: '10px' }}>
                      {session.user?.firstName} {session.user?.lastName}
                    </td>
                    <td style={{ padding: '10px' }}>{session.category?.name || 'General'}</td>
                    <td style={{ padding: '10px' }}>
                      <span style={{ 
                        padding: '4px 8px', 
                        borderRadius: '4px', 
                        backgroundColor: getStatusColor(session.status),
                        color: 'white',
                        fontSize: '12px'
                      }}>
                        {session.status}
                      </span>
                    </td>
                    <td style={{ padding: '10px' }}>
                      {new Date(session.createdAt).toLocaleDateString()}
                    </td>
                    <td style={{ padding: '10px' }}>
                      {formatDuration(session.startedAt, session.endedAt)}
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

export default HelperSessions
