import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { helperService } from '../../services/helperService'
import { sessionService } from '../../services/sessionService'
import Navbar from '../../components/Navbar'

const HelperDashboard = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [helper, setHelper] = useState(null)
  const [sessions, setSessions] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadHelper()
    loadSessions()
    // Poll for new sessions every 5 seconds
    const interval = setInterval(loadSessions, 5000)
    return () => clearInterval(interval)
  }, [])

  const loadHelper = async () => {
    try {
      const data = await helperService.getHelperByUserId(user.id)
      setHelper(data)
    } catch (err) {
      console.error('Failed to load helper data', err)
    } finally {
      setLoading(false)
    }
  }

  const loadSessions = async () => {
    try {
      const data = await helperService.getHelperByUserId(user.id)
      if (data?.id) {
        const sessionsData = await helperService.getHelperSessions(data.id)
        setSessions(sessionsData)
      }
    } catch (err) {
      console.error('Failed to load sessions', err)
    }
  }

  const toggleAvailability = async () => {
    try {
      const updated = await helperService.updateAvailability(helper.id, !helper.isAvailable)
      setHelper(updated)
    } catch (err) {
      alert('Failed to update availability')
    }
  }

  const handleAcceptSession = async (sessionId) => {
    try {
      await sessionService.acceptSession(sessionId)
      alert('Session accepted!')
      loadSessions()
      navigate(`/session/${sessionId}`)
    } catch (err) {
      alert('Failed to accept session: ' + (err.response?.data?.message || err.message))
    }
  }

  const handleRejectSession = async (sessionId) => {
    try {
      await sessionService.rejectSession(sessionId)
      alert('Session rejected')
      loadSessions()
    } catch (err) {
      alert('Failed to reject session: ' + (err.response?.data?.message || err.message))
    }
  }

  const pendingSessions = sessions.filter(s => s.status === 'INITIATED')
  const activeSessions = sessions.filter(s => s.status === 'CONNECTED' || s.status === 'IN_PROGRESS')

  if (loading) return <><Navbar /><div className="loading">Loading...</div></>

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Helper Dashboard</h1>

        {pendingSessions.length > 0 && (
          <div className="card" style={{ backgroundColor: '#fff3cd', borderColor: '#ffc107' }}>
            <h2>🔔 Pending Session Requests ({pendingSessions.length})</h2>
            {pendingSessions.map(session => (
              <div key={session.id} style={{ 
                padding: '15px', 
                marginBottom: '10px', 
                border: '1px solid #ddd', 
                borderRadius: '4px',
                backgroundColor: 'white'
              }}>
                <p><strong>Session ID:</strong> {session.id}</p>
                <p><strong>User:</strong> {session.user?.firstName} {session.user?.lastName}</p>
                <p><strong>Category:</strong> {session.category?.name || 'General'}</p>
                <p><strong>Requested:</strong> {new Date(session.createdAt).toLocaleString()}</p>
                <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
                  <button 
                    onClick={() => handleAcceptSession(session.id)} 
                    className="btn btn-primary"
                  >
                    Accept Session
                  </button>
                  <button 
                    onClick={() => handleRejectSession(session.id)} 
                    className="btn btn-danger"
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {activeSessions.length > 0 && (
          <div className="card">
            <h2>Active Sessions ({activeSessions.length})</h2>
            {activeSessions.map(session => (
              <div key={session.id} style={{ 
                padding: '15px', 
                marginBottom: '10px', 
                border: '1px solid #ddd', 
                borderRadius: '4px'
              }}>
                <p><strong>Session ID:</strong> {session.id}</p>
                <p><strong>User:</strong> {session.user?.firstName} {session.user?.lastName}</p>
                <p><strong>Status:</strong> {session.status}</p>
                <button 
                  onClick={() => navigate(`/session/${session.id}`)} 
                  className="btn btn-primary"
                >
                  Join Session
                </button>
              </div>
            ))}
          </div>
        )}
        
        <div className="card">
          <h2>Availability Status</h2>
          <p>You are currently: <strong>{helper?.isAvailable ? 'Available' : 'Unavailable'}</strong></p>
          <button onClick={toggleAvailability} className="btn btn-primary">
            {helper?.isAvailable ? 'Set Unavailable' : 'Set Available'}
          </button>
        </div>

        <div className="card">
          <h2>Quick Actions</h2>
          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <Link to="/helper/profile" className="btn btn-primary">Manage Profile</Link>
            <Link to="/helper/earnings" className="btn btn-secondary">View Earnings</Link>
          </div>
        </div>

        <div className="card">
          <h2>Profile Summary</h2>
          <p><strong>Headline:</strong> {helper?.professionalHeadline || 'Not set'}</p>
          <p><strong>Languages:</strong> {helper?.languagesSpoken || 'Not set'}</p>
          <p><strong>Specializations:</strong> {helper?.helperCategories?.length || 0}</p>
        </div>
      </div>
    </>
  )
}

export default HelperDashboard
