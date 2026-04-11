import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { sessionService } from '../../services/sessionService'
import Navbar from '../../components/Navbar'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

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
  const stompClientRef = useRef(null)
  const subscriptionRef = useRef(null)
  const peerConnectionRef = useRef(null)
  const localStreamRef = useRef(null)

  useEffect(() => {
    let mounted = true
    const currentSessionId = sessionId // Capture sessionId for cleanup
    
    const initialize = async () => {
      if (!mounted) return
      
      // Clean up any existing connections first
      cleanupConnections(currentSessionId)
      
      const sessionData = await loadSession()
      
      // Only initialize media and WebSocket for active sessions
      if (sessionData && (sessionData.status === 'INITIATED' || sessionData.status === 'CONNECTED' || sessionData.status === 'IN_PROGRESS')) {
        const mediaInitialized = await initializeMedia()
        
        // Only connect WebSocket if media was successfully initialized
        if (mediaInitialized && mounted) {
          // Small delay to ensure media is ready before connecting WebSocket
          setTimeout(() => {
            if (mounted) {
              connectWebSocket()
            }
          }, 1000)
        }
      }
    }
    
    initialize()
    
    // Handle page unload/refresh
    const handleBeforeUnload = () => {
      cleanupConnections(currentSessionId)
    }
    
    window.addEventListener('beforeunload', handleBeforeUnload)
    
    return () => {
      mounted = false
      cleanupConnections(currentSessionId)
      window.removeEventListener('beforeunload', handleBeforeUnload)
    }
  }, [sessionId])

  const cleanupConnections = (currentSessionId) => {
    console.log('Cleaning up connections for session:', currentSessionId)
    
    // Unsubscribe from WebSocket topic
    if (subscriptionRef.current) {
      try {
        console.log('Unsubscribing from WebSocket topic')
        subscriptionRef.current.unsubscribe()
        subscriptionRef.current = null
      } catch (err) {
        console.error('Error unsubscribing:', err)
      }
    }
    
    // Notify that user is leaving
    if (stompClientRef.current && stompClientRef.current.connected && currentSessionId) {
      try {
        stompClientRef.current.publish({
          destination: `/app/session/${currentSessionId}/leave`,
          body: JSON.stringify({
            userId: user.id
          })
        })
      } catch (err) {
        console.error('Error notifying leave:', err)
      }
    }
    
    // Cleanup WebSocket
    if (stompClientRef.current) {
      try {
        stompClientRef.current.deactivate()
        stompClientRef.current = null
      } catch (err) {
        console.error('Error deactivating STOMP client:', err)
      }
    }
    
    // Cleanup peer connection
    if (peerConnectionRef.current) {
      try {
        peerConnectionRef.current.close()
        peerConnectionRef.current = null
      } catch (err) {
        console.error('Error closing peer connection:', err)
      }
    }
    
    // Cleanup local media stream
    if (localStreamRef.current) {
      try {
        localStreamRef.current.getTracks().forEach(track => {
          track.stop()
          console.log('Stopped track:', track.kind, track.id)
        })
        localStreamRef.current = null
      } catch (err) {
        console.error('Error stopping media tracks:', err)
      }
    }
    
    // Clear video elements
    if (localVideoRef.current) {
      localVideoRef.current.srcObject = null
      localVideoRef.current.load()
    }
    if (remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = null
      remoteVideoRef.current.load()
    }
    
    console.log('Cleanup complete')
  }

  const connectWebSocket = () => {
    const socket = new SockJS('http://localhost:8080/ws')
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('WebSocket connected for user:', user.id, 'userType:', user.userType)
        
        // Subscribe to session updates and store subscription reference
        subscriptionRef.current = client.subscribe(`/topic/session/${sessionId}`, (message) => {
          const data = JSON.parse(message.body)
          console.log('Received WebSocket message:', data)
          
          if (data.type === 'CHAT_MESSAGE') {
            setMessages(prev => [...prev, data.message])
          } else if (data.type === 'SESSION_ENDED') {
            alert('Session has been ended by the other participant')
            navigate('/user/sessions')
          } else if (data.type === 'SESSION_STATUS_UPDATE') {
            console.log('Session status updated to:', data.status)
            // Reload session data to get updated status
            loadSession()
          } else if (data.type === 'user-joined') {
            console.log('User joined:', data.userName, 'My ID:', user.id, 'Their ID:', data.senderId, 'My Type:', user.userType)
            
            // Ignore if it's my own join message
            if (data.senderId === user.id) {
              console.log('Ignoring my own join message')
              return
            }
            
            // Verify local media is ready before creating offer
            if (!localStreamRef.current || localStreamRef.current.getTracks().length === 0) {
              console.warn('Local media not ready when user joined, waiting...')
              setTimeout(() => {
                if (localStreamRef.current && localStreamRef.current.getTracks().length > 0) {
                  console.log('Local media now ready, proceeding with connection')
                  handleUserJoined()
                } else {
                  console.error('Local media still not ready after delay')
                }
              }, 2000)
              return
            }
            
            handleUserJoined()
            
            function handleUserJoined() {
              // Only USER creates offer, HELPER waits for offer
              if (user.userType === 'USER') {
                console.log('I am USER, will create offer...')
                // Close existing connection if any
                if (peerConnectionRef.current) {
                  console.log('Closing existing peer connection')
                  peerConnectionRef.current.close()
                  peerConnectionRef.current = null
                }
                // Wait a bit to ensure both sides are ready
                setTimeout(() => {
                  console.log('Creating offer now...')
                  createOffer()
                }, 2000)
              } else {
                console.log('I am HELPER, waiting for offer from USER...')
              }
            }
          } else if (data.type === 'offer') {
            console.log('Received offer from:', data.senderId, 'My ID:', user.id)
            if (data.senderId !== user.id) {
              handleOffer(data)
            }
          } else if (data.type === 'answer') {
            console.log('Received answer from:', data.senderId, 'My ID:', user.id)
            if (data.senderId !== user.id) {
              handleAnswer(data)
            }
          } else if (data.type === 'ice-candidate') {
            console.log('Received ICE candidate from:', data.senderId)
            if (data.senderId !== user.id) {
              handleIceCandidate(data)
            }
          } else if (data.type === 'user-left') {
            console.log('User left:', data.senderId)
            // Close peer connection when other user leaves
            if (peerConnectionRef.current) {
              peerConnectionRef.current.close()
              peerConnectionRef.current = null
            }
            // Clear remote video
            if (remoteVideoRef.current) {
              remoteVideoRef.current.srcObject = null
            }
          }
        })
        
        // Notify that user joined
        console.log('Notifying that I joined:', user.id, user.userType)
        client.publish({
          destination: `/app/session/${sessionId}/join`,
          body: JSON.stringify({
            userId: user.id,
            userName: `${user.firstName} ${user.lastName}`,
            userType: user.userType
          })
        })
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
      }
    })
    
    client.activate()
    stompClientRef.current = client
  }

  const createPeerConnection = () => {
    // Close existing connection if any
    if (peerConnectionRef.current) {
      console.log('Closing existing peer connection before creating new one')
      peerConnectionRef.current.close()
      peerConnectionRef.current = null
    }

    const configuration = {
      iceServers: [
        { urls: 'stun:stun.l.google.com:19302' },
        { urls: 'stun:stun1.l.google.com:19302' }
      ]
    }

    const pc = new RTCPeerConnection(configuration)

    // Add local stream to peer connection
    if (localStreamRef.current) {
      localStreamRef.current.getTracks().forEach(track => {
        console.log('Adding track to peer connection:', track.kind)
        pc.addTrack(track, localStreamRef.current)
      })
    } else {
      console.warn('No local stream available when creating peer connection')
    }

    // Handle incoming remote stream
    pc.ontrack = (event) => {
      console.log('Received remote track:', event.streams[0].id, 'tracks:', event.streams[0].getTracks().length)
      if (remoteVideoRef.current) {
        remoteVideoRef.current.srcObject = event.streams[0]
        // Ensure remote video plays
        remoteVideoRef.current.onloadedmetadata = () => {
          remoteVideoRef.current.play().catch(err => {
            console.warn('Remote video autoplay prevented:', err)
            // Try again with user interaction
            document.addEventListener('click', () => {
              remoteVideoRef.current.play().catch(e => console.error('Remote play failed:', e))
            }, { once: true })
          })
        }
      }
    }

    // Handle ICE candidates
    pc.onicecandidate = (event) => {
      if (event.candidate && stompClientRef.current) {
        console.log('Sending ICE candidate')
        stompClientRef.current.publish({
          destination: `/app/session/${sessionId}/ice-candidate`,
          body: JSON.stringify({
            candidate: event.candidate,
            senderId: user.id
          })
        })
      }
    }

    pc.onconnectionstatechange = () => {
      console.log('Connection state:', pc.connectionState)
      if (pc.connectionState === 'failed' || pc.connectionState === 'disconnected') {
        console.warn('Peer connection failed or disconnected')
      }
    }

    pc.oniceconnectionstatechange = () => {
      console.log('ICE connection state:', pc.iceConnectionState)
    }

    peerConnectionRef.current = pc
    return pc
  }

  const createOffer = async () => {
    try {
      console.log('=== Creating offer ===')
      console.log('User ID:', user.id, 'Type:', user.userType)
      console.log('Session ID:', sessionId)
      console.log('Local stream exists:', !!localStreamRef.current)
      console.log('Local stream tracks:', localStreamRef.current?.getTracks().length)
      
      // Ensure we have local media before creating offer
      if (!localStreamRef.current || localStreamRef.current.getTracks().length === 0) {
        console.error('No local stream available, reinitializing media...')
        await initializeMedia()
        // Wait a bit after initializing
        await new Promise(resolve => setTimeout(resolve, 500))
      }
      
      console.log('Creating peer connection...')
      const pc = createPeerConnection()
      
      console.log('Creating offer...')
      const offer = await pc.createOffer({
        offerToReceiveAudio: true,
        offerToReceiveVideo: true
      })
      
      console.log('Setting local description...')
      await pc.setLocalDescription(offer)
      
      console.log('Offer created and set as local description')
      console.log('Offer SDP length:', offer.sdp.length)

      if (stompClientRef.current && stompClientRef.current.connected) {
        console.log('Sending offer via WebSocket...')
        stompClientRef.current.publish({
          destination: `/app/session/${sessionId}/offer`,
          body: JSON.stringify({
            sdp: offer.sdp,
            senderId: user.id
          })
        })
        console.log('✅ Offer sent successfully')
      } else {
        console.error('❌ Cannot send offer: WebSocket not connected')
      }
    } catch (err) {
      console.error('❌ Error creating offer:', err)
    }
  }

  const handleOffer = async (data) => {
    try {
      console.log('=== Handling offer from:', data.senderId, '===')
      console.log('My user ID:', user.id, 'Type:', user.userType)
      console.log('Offer SDP length:', data.sdp?.length)
      
      // Ensure we have local media before handling offer
      if (!localStreamRef.current || localStreamRef.current.getTracks().length === 0) {
        console.error('No local stream available, reinitializing media...')
        await initializeMedia()
        // Wait a bit after initializing
        await new Promise(resolve => setTimeout(resolve, 500))
      }
      
      console.log('Local stream tracks:', localStreamRef.current.getTracks().length)
      
      // Close existing peer connection if any
      if (peerConnectionRef.current) {
        console.log('Closing existing peer connection before handling offer')
        peerConnectionRef.current.close()
        peerConnectionRef.current = null
      }
      
      console.log('Creating peer connection...')
      const pc = createPeerConnection()
      
      console.log('Setting remote description from offer...')
      await pc.setRemoteDescription(new RTCSessionDescription({ type: 'offer', sdp: data.sdp }))
      console.log('✅ Remote description set from offer')
      
      console.log('Creating answer...')
      const answer = await pc.createAnswer()
      
      console.log('Setting local description...')
      await pc.setLocalDescription(answer)
      console.log('✅ Answer created and set as local description')
      console.log('Answer SDP length:', answer.sdp.length)

      if (stompClientRef.current && stompClientRef.current.connected) {
        console.log('Sending answer via WebSocket...')
        stompClientRef.current.publish({
          destination: `/app/session/${sessionId}/answer`,
          body: JSON.stringify({
            sdp: answer.sdp,
            senderId: user.id
          })
        })
        console.log('✅ Answer sent successfully')
      } else {
        console.error('❌ Cannot send answer: WebSocket not connected')
      }
    } catch (err) {
      console.error('❌ Error handling offer:', err)
      console.error('Error details:', err.message)
    }
  }

  const handleAnswer = async (data) => {
    try {
      console.log('=== Handling answer from:', data.senderId, '===')
      console.log('My user ID:', user.id)
      console.log('Answer SDP length:', data.sdp?.length)
      console.log('Peer connection exists:', !!peerConnectionRef.current)
      console.log('Signaling state:', peerConnectionRef.current?.signalingState)
      
      if (peerConnectionRef.current && peerConnectionRef.current.signalingState !== 'stable') {
        console.log('Setting remote description from answer...')
        await peerConnectionRef.current.setRemoteDescription(
          new RTCSessionDescription({ type: 'answer', sdp: data.sdp })
        )
        console.log('✅ Remote description set from answer')
        console.log('New signaling state:', peerConnectionRef.current.signalingState)
      } else {
        console.warn('⚠️ Cannot set answer - peer connection not in correct state')
      }
    } catch (err) {
      console.error('❌ Error handling answer:', err)
      console.error('Error details:', err.message)
    }
  }

  const handleIceCandidate = async (data) => {
    try {
      if (data.senderId === user.id) return // Ignore own candidates

      console.log('Received ICE candidate')
      if (peerConnectionRef.current && data.candidate) {
        await peerConnectionRef.current.addIceCandidate(new RTCIceCandidate(data.candidate))
      }
    } catch (err) {
      console.error('Error handling ICE candidate:', err)
    }
  }

  const loadSession = async () => {
    try {
      const data = await sessionService.getSessionLog(sessionId)
      setSession(data.session)
      setMessages(data.messages || [])
      return data.session
    } catch (err) {
      console.error('Failed to load session', err)
      return null
    } finally {
      setLoading(false)
    }
  }

  const initializeMedia = async () => {
    try {
      // Stop any existing streams first
      if (localStreamRef.current) {
        localStreamRef.current.getTracks().forEach(track => track.stop())
      }
      
      console.log('Requesting camera and microphone access...')
      
      // Request fresh media stream
      const stream = await navigator.mediaDevices.getUserMedia({ 
        video: {
          width: { ideal: 1280 },
          height: { ideal: 720 }
        }, 
        audio: {
          echoCancellation: true,
          noiseSuppression: true
        }
      })
      
      localStreamRef.current = stream
      if (localVideoRef.current) {
        localVideoRef.current.srcObject = stream
        // Ensure video plays - critical for mobile and some browsers
        localVideoRef.current.onloadedmetadata = () => {
          localVideoRef.current.play().catch(err => {
            console.warn('Local video autoplay prevented:', err)
            // Try again with user interaction
            document.addEventListener('click', () => {
              localVideoRef.current.play().catch(e => console.error('Play failed:', e))
            }, { once: true })
          })
        }
      }
      console.log('✅ Local media initialized with', stream.getTracks().length, 'tracks')
      stream.getTracks().forEach(track => {
        console.log('  - Track:', track.kind, 'enabled:', track.enabled, 'readyState:', track.readyState, 'label:', track.label)
      })
      
      return true // Return success
    } catch (err) {
      console.error('❌ Failed to access media devices:', err.name, err.message)
      
      // Show user-friendly error message
      let errorMessage = '⚠️ Media Access Error\n\n'
      
      if (err.name === 'NotFoundError' || err.name === 'DevicesNotFoundError') {
        errorMessage += 'Camera/Microphone Not Found\n\n'
        errorMessage += 'Your device does not have a camera or microphone, or they are not connected.\n\n'
        errorMessage += 'To test video sessions:\n'
        errorMessage += '1. Connect a webcam and microphone\n'
        errorMessage += '2. Or use a device with built-in camera (laptop, phone)\n'
        errorMessage += '3. Refresh the page after connecting devices'
      } else if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
        errorMessage += 'Permission Denied\n\n'
        errorMessage += 'Please allow camera and microphone access:\n'
        errorMessage += '1. Click the camera icon in your browser address bar\n'
        errorMessage += '2. Select "Allow" for camera and microphone\n'
        errorMessage += '3. Refresh the page'
      } else {
        errorMessage += err.message + '\n\n'
        errorMessage += 'Please check:\n'
        errorMessage += '1. Camera/microphone are connected\n'
        errorMessage += '2. No other app is using them\n'
        errorMessage += '3. Browser has permission to access them'
      }
      
      alert(errorMessage)
      
      return false // Return failure
    }
  }

  const handleSendMessage = async (e) => {
    e.preventDefault()
    if (!newMessage.trim()) return

    try {
      const message = await sessionService.sendMessage(sessionId, {
        userId: user.id,
        messageText: newMessage
      })
      setNewMessage('')
      
      // Broadcast via WebSocket
      if (stompClientRef.current && stompClientRef.current.connected) {
        stompClientRef.current.publish({
          destination: `/app/session/${sessionId}/chat`,
          body: JSON.stringify({
            type: 'CHAT_MESSAGE',
            message: message
          })
        })
      }
    } catch (err) {
      console.error('Send message error:', err)
      alert('Failed to send message: ' + (err.response?.data?.message || err.message))
    }
  }

  const handleEndSession = async () => {
    if (!confirm('Are you sure you want to end this session?')) return

    try {
      // Notify other user via WebSocket first
      if (stompClientRef.current && stompClientRef.current.connected) {
        stompClientRef.current.publish({
          destination: `/app/session/${sessionId}/end`,
          body: JSON.stringify({
            type: 'SESSION_ENDED',
            userId: user.id
          })
        })
      }
      
      // Clean up connections before ending
      cleanupConnections(sessionId)
      
      await sessionService.endSession(sessionId)
      
      alert('Session ended successfully')
      navigate('/user/sessions')
    } catch (err) {
      console.error('End session error:', err)
      alert('Failed to end session: ' + (err.response?.data?.message || err.message))
    }
  }

  if (loading) return <><Navbar /><div className="loading">Loading...</div></>

  const isSessionEnded = session?.status === 'ENDED' || session?.status === 'CANCELLED' || session?.status === 'REJECTED'

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Video Session: {sessionId}</h1>
        
        {isSessionEnded && (
          <div className="card" style={{ backgroundColor: '#f8f9fa', borderLeft: '4px solid #6c757d' }}>
            <h3>📋 Session History View</h3>
            <p>This session has ended. You are viewing the chat history and session details.</p>
          </div>
        )}
        
        <div style={{ display: 'grid', gridTemplateColumns: isSessionEnded ? '1fr' : '2fr 1fr', gap: '20px' }}>
          {!isSessionEnded && (
            <div>
              <div className="card">
                <h3>Video Call</h3>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                  <div>
                    <p>Your Video</p>
                    <video
                      ref={localVideoRef}
                      autoPlay
                      playsInline
                      muted
                      style={{ width: '100%', maxHeight: '400px', background: '#000', borderRadius: '4px' }}
                    />
                  </div>
                  <div>
                    <p>Remote Video</p>
                    <video
                      ref={remoteVideoRef}
                      autoPlay
                      playsInline
                      style={{ width: '100%', maxHeight: '400px', background: '#000', borderRadius: '4px' }}
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
          )}

          <div>
            <div className="card">
              <h3>Chat {isSessionEnded && 'History'}</h3>
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
              {!isSessionEnded && (
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
              )}
            </div>

            <div className="card">
              <h3>Session Info</h3>
              <p><strong>Status:</strong> {session?.status}</p>
              <p><strong>Started:</strong> {session?.startedAt ? new Date(session.startedAt).toLocaleString() : 'Not started'}</p>
              {session?.endedAt && (
                <>
                  <p><strong>Ended:</strong> {new Date(session.endedAt).toLocaleString()}</p>
                  <p><strong>Duration:</strong> {(() => {
                    const start = new Date(session.startedAt)
                    const end = new Date(session.endedAt)
                    const diffMs = end - start
                    const diffMins = Math.floor(diffMs / 60000)
                    const diffSecs = Math.floor((diffMs % 60000) / 1000)
                    return diffMins > 0 ? `${diffMins} min ${diffSecs} sec` : `${diffSecs} sec`
                  })()}</p>
                </>
              )}
              {session?.category && (
                <p><strong>Category:</strong> {session.category.name}</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default VideoSession
