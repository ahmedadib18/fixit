import { useState, useEffect, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { helperService } from '../../services/helperService'
import { sessionService } from '../../services/sessionService'
import Navbar from '../../components/Navbar'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const HelperDashboard = () => {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [helper, setHelper] = useState(null)
  const [sessions, setSessions] = useState([])
  const [loading, setLoading] = useState(true)
  const stompClientRef = useRef(null)

  useEffect(() => {
    loadHelper()
    loadSessions()
    connectWebSocket()
    
    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.deactivate()
      }
    }
  }, [])

  const connectWebSocket = () => {
    const socket = new SockJS('http://localhost:8080/ws')
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('Helper Dashboard WebSocket connected for user:', user.id)
        
        // Get helper ID first, then subscribe
        helperService.getHelperByUserId(user.id).then(helperData => {
          if (helperData && helperData.id) {
            const helperId = helperData.id
            console.log('Subscribing to notifications for helper ID:', helperId)
            
            // Subscribe to helper-specific notifications using helper ID
            client.subscribe(`/topic/helper/${helperId}/notifications`, (message) => {
              const data = JSON.parse(message.body)
              console.log('✅ Received notification:', data)
              
              if (data.type === 'NEW_SESSION_REQUEST') {
                console.log('🔔 New session request received, reloading sessions...')
                
                // Play notification sound
                playNotificationSound()
                
                // Show browser notification if permitted
                showBrowserNotification('New Session Request', 
                  `${data.userName} is requesting a session for ${data.categoryName}`)
                
                // Force reload sessions to show the new request
                setTimeout(() => {
                  loadSessions()
                }, 500) // Small delay to ensure backend has saved the session
              }
            })
          }
        }).catch(err => {
          console.error('Failed to get helper ID for WebSocket subscription:', err)
        })
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
      }
    })
    
    client.activate()
    stompClientRef.current = client
  }

  const playNotificationSound = () => {
    // Create a simple beep sound
    const audioContext = new (window.AudioContext || window.webkitAudioContext)()
    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()
    
    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)
    
    oscillator.frequency.value = 800
    oscillator.type = 'sine'
    
    gainNode.gain.setValueAtTime(0.3, audioContext.currentTime)
    gainNode.gain.exponentialRampToValueAtTime(0.01, audioContext.currentTime + 0.5)
    
    oscillator.start(audioContext.currentTime)
    oscillator.stop(audioContext.currentTime + 0.5)
  }

  const showBrowserNotification = (title, body) => {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(title, { body, icon: '/favicon.ico' })
    } else if ('Notification' in window && Notification.permission !== 'denied') {
      Notification.requestPermission().then(permission => {
        if (permission === 'granted') {
          new Notification(title, { body, icon: '/favicon.ico' })
        }
      })
    }
  }

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
        console.log('Loaded sessions:', sessionsData?.length || 0)
        setSessions(sessionsData || [])
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
            <Link to="/helper/sessions" className="btn btn-secondary">View Sessions</Link>
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
