import { useState, useEffect, useRef } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { userService } from '../../services/userService'
import { billingService } from '../../services/billingService'
import Navbar from '../../components/Navbar'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

const UserSessions = () => {
  const { user } = useAuth()
  const [sessions, setSessions] = useState([])
  const [transactions, setTransactions] = useState({})
  const [loading, setLoading] = useState(true)
  const stompClientRef = useRef(null)

  useEffect(() => {
    loadData()
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
        console.log('User Sessions WebSocket connected')
        
        // Subscribe to user-specific session updates
        client.subscribe(`/topic/user/${user.id}/session-updates`, (message) => {
          const data = JSON.parse(message.body)
          console.log('Received session update:', data)
          
          if (data.type === 'SESSION_STATUS_UPDATE') {
            // Update the specific session in the list
            setSessions(prevSessions => 
              prevSessions.map(session => 
                session.id === data.sessionId 
                  ? { ...session, status: data.status }
                  : session
              )
            )
          }
        })
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
      }
    })
    
    client.activate()
    stompClientRef.current = client
  }

  const loadData = async () => {
    try {
      const sessionsData = await userService.getSessions(user.id)
      setSessions(sessionsData)
      
      // Load transactions for user
      const transactionsData = await billingService.getUserTransactions(user.id)
      
      // Create a map of sessionId -> transaction
      const transactionMap = {}
      transactionsData.forEach(t => {
        transactionMap[t.sessionId] = t
      })
      setTransactions(transactionMap)
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
                  <th style={{ padding: '10px', textAlign: 'left' }}>Duration</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Amount</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {sessions.map(session => {
                  const transaction = transactions[session.id]
                  return (
                    <tr key={session.id} style={{ borderBottom: '1px solid #eee' }}>
                      <td style={{ padding: '10px' }}>{session.id}</td>
                      <td style={{ padding: '10px' }}>
                        <span style={{
                          padding: '4px 8px',
                          borderRadius: '4px',
                          backgroundColor: session.status === 'ENDED' ? '#28a745' : '#ffc107',
                          color: 'white',
                          fontSize: '12px'
                        }}>
                          {session.status}
                        </span>
                      </td>
                      <td style={{ padding: '10px' }}>
                        {session.startedAt ? new Date(session.startedAt).toLocaleString() : 'Not started'}
                      </td>
                      <td style={{ padding: '10px' }}>
                        {formatDuration(session.startedAt, session.endedAt)}
                      </td>
                      <td style={{ padding: '10px' }}>
                        {transaction ? (
                          <span>
                            ${parseFloat(transaction.amount).toFixed(2)}
                            <br />
                            <small style={{ color: '#666' }}>({transaction.status})</small>
                          </span>
                        ) : (
                          <span style={{ color: '#999' }}>-</span>
                        )}
                      </td>
                      <td style={{ padding: '10px' }}>
                        <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                          <Link to={`/session/${session.id}`} className="btn btn-primary" style={{ padding: '5px 10px' }}>
                            View
                          </Link>
                          {session.status === 'ENDED' && (
                            <Link to={`/user/review/${session.id}`} className="btn btn-secondary" style={{ padding: '5px 10px' }}>
                              Review
                            </Link>
                          )}
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </>
  )
}

export default UserSessions
