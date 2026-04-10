import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { adminService } from '../../services/adminService'
import Navbar from '../../components/Navbar'

const ManageTickets = () => {
  const { user } = useAuth()
  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadTickets()
  }, [])

  const loadTickets = async () => {
    try {
      const data = await adminService.getAllTickets()
      setTickets(data)
    } catch (err) {
      console.error('Failed to load tickets', err)
    } finally {
      setLoading(false)
    }
  }

  const handleResponse = async (ticketId) => {
    const responseText = prompt('Enter your response:')
    if (!responseText) return

    try {
      await adminService.addTicketResponse(ticketId, user.id, responseText)
      loadTickets()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to add response')
    }
  }

  const handleClose = async (ticketId) => {
    try {
      await adminService.closeTicket(ticketId)
      loadTickets()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to close ticket')
    }
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Manage Support Tickets</h1>
        <div className="card">
          {loading ? (
            <div className="loading">Loading...</div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                  <th style={{ padding: '10px', textAlign: 'left' }}>ID</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Subject</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Status</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Created</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {tickets.map(ticket => (
                  <tr key={ticket.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '10px' }}>{ticket.id}</td>
                    <td style={{ padding: '10px' }}>{ticket.subject}</td>
                    <td style={{ padding: '10px' }}>{ticket.status}</td>
                    <td style={{ padding: '10px' }}>
                      {new Date(ticket.createdAt).toLocaleDateString()}
                    </td>
                    <td style={{ padding: '10px' }}>
                      <button onClick={() => handleResponse(ticket.id)} className="btn btn-primary" style={{ padding: '5px 10px', marginRight: '5px' }}>
                        Respond
                      </button>
                      {ticket.status !== 'CLOSED' && (
                        <button onClick={() => handleClose(ticket.id)} className="btn btn-secondary" style={{ padding: '5px 10px' }}>
                          Close
                        </button>
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

export default ManageTickets
