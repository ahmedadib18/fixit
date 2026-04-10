import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import { supportService } from '../../services/supportService'
import Navbar from '../../components/Navbar'

const SubmitTicket = () => {
  const { user } = useAuth()
  const [formData, setFormData] = useState({
    subject: '',
    description: '',
    sessionId: ''
  })
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setMessage('')
    setLoading(true)

    try {
      await supportService.createTicket({
        userId: user.id,
        ...formData,
        sessionId: formData.sessionId || null
      })
      setMessage('Support ticket submitted successfully!')
      setFormData({ subject: '', description: '', sessionId: '' })
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to submit ticket')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <div className="card" style={{ maxWidth: '600px', margin: '0 auto' }}>
          <h2>Submit Support Ticket</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Subject</label>
              <input
                type="text"
                name="subject"
                value={formData.subject}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea
                name="description"
                value={formData.description}
                onChange={handleChange}
                rows="6"
                required
              />
            </div>
            <div className="form-group">
              <label>Session ID (Optional)</label>
              <input
                type="text"
                name="sessionId"
                value={formData.sessionId}
                onChange={handleChange}
                placeholder="If related to a specific session"
              />
            </div>
            {message && <div className={message.includes('success') ? 'success' : 'error'}>{message}</div>}
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Submitting...' : 'Submit Ticket'}
            </button>
          </form>
        </div>
      </div>
    </>
  )
}

export default SubmitTicket
