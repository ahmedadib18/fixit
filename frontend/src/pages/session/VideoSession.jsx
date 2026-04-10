import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { sessionService } from '../../services/sessionService'
import Navbar from '../../components/Navbar'

const VideoSession = () => {
  const { sessionId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [session, setSession] = useState(null)
  const [messages, setMessages] = useState([])
  const [newMessage, setNewMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const localVideoRef = useRef(null)
  const remoteVideoRef = useRef(null)

  useEffect(() => {
    loadSession()
    initializeMedia()
  }, [sessionId])

  const loadSession = async () => {
    try {
      const data = await sessionService.getSessionLog(sessionId)
      setSession(data.session)
      setMessages(data.messages || [])
    } catch (err) {
      console.error('Failed to load session', err)
    } finally {
      setLoading(false)
    }
  }

  const initializeMedia = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true })
      if (localVideoRef.current) {
        localVideoRef.current.srcObject = stream
      }
    } catch (err) {
      console.error('Failed to access media devices', err)
      alert('Please allow camera and microphone access')
    }
  }

  const handleSendMessage = async (e) => {
    e.preventDefault()
    if (!newMessage.trim()) return

    try {
      await sessionService.sendMessage(sessionId, {
        userId: user.id,
        messageText: newMessage
      })
      setNewMessage('')
      // Reload session to get updated messages
      await loadSession()
    } catch (err) {
      console.error('Send message error:', err)
      alert('Failed to send message: ' + (err.response?.data?.message || err.message))
    }
  }

  const handleEndSession = async () => {
    if (!confirm('Are you sure you want to end this session?')) return

    try {
      await sessionService.endSession(sessionId)
      alert('Session ended successfully')
      navigate('/user/sessions')
    } catch (err) {
      console.error('End session error:', err)
      alert('Failed to end session: ' + (err.response?.data?.message || err.message))
    }
  }

  if (loading) return <><Navbar /><div className="loading">Loading...</div></>

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Video Session: {sessionId}</h1>
        
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '20px' }}>
          <div>
            <div className="card">
              <h3>Video Call</h3>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div>
                  <p>Your Video</p>
                  <video
                    ref={localVideoRef}
                    autoPlay
                    muted
                    style={{ width: '100%', background: '#000', borderRadius: '4px' }}
                  />
                </div>
                <div>
                  <p>Remote Video</p>
                  <video
                    ref={remoteVideoRef}
                    autoPlay
                    style={{ width: '100%', background: '#000', borderRadius: '4px' }}
                  />
                </div>
              </div>
              <div style={{ marginTop: '15px', display: 'flex', gap: '10px' }}>
                <button className="btn btn-danger" onClick={handleEndSession}>
                  End Session
                </button>
              </div>
            </div>
          </div>

          <div>
            <div className="card">
              <h3>Chat</h3>
              <div style={{ height: '300px', overflowY: 'auto', border: '1px solid #ddd', padding: '10px', marginBottom: '10px', borderRadius: '4px' }}>
                {messages.length === 0 ? (
                  <p style={{ color: '#999', textAlign: 'center' }}>No messages yet</p>
                ) : (
                  messages.map(msg => (
                    <div key={msg.id} style={{ marginBottom: '10px' }}>
                      <strong>
                        {msg.sender?.id === user.id 
                          ? 'You' 
                          : `${msg.sender?.firstName || 'Other'} ${msg.sender?.lastName || ''}`}:
                      </strong> {msg.messageText}
                      <div style={{ fontSize: '0.8em', color: '#999' }}>
                        {msg.sentAt ? new Date(msg.sentAt).toLocaleTimeString() : ''}
                      </div>
                    </div>
                  ))
                )}
              </div>
              <form onSubmit={handleSendMessage}>
                <div style={{ display: 'flex', gap: '10px' }}>
                  <input
                    type="text"
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    placeholder="Type a message..."
                    style={{ flex: 1, padding: '10px', border: '1px solid #ddd', borderRadius: '4px' }}
                  />
                  <button type="submit" className="btn btn-primary">Send</button>
                </div>
              </form>
            </div>

            <div className="card">
              <h3>Session Info</h3>
              <p><strong>Status:</strong> {session?.status}</p>
              <p><strong>Started:</strong> {session?.startedAt ? new Date(session.startedAt).toLocaleString() : 'Not started'}</p>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default VideoSession
