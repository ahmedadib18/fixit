import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { reviewService } from '../../services/reviewService'
import { sessionService } from '../../services/sessionService'
import Navbar from '../../components/Navbar'

const SubmitReview = () => {
  const { sessionId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [session, setSession] = useState(null)
  const [rating, setRating] = useState(0)
  const [reviewText, setReviewText] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    loadSession()
  }, [sessionId])

  const loadSession = async () => {
    try {
      const data = await sessionService.getSessionLog(sessionId)
      console.log('Loaded session data:', data)
      
      if (!data || !data.session) {
        setError('Failed to load session details')
        setLoading(false)
        return
      }
      
      setSession(data.session)
      
      // Check if session is ended
      if (data.session.status !== 'ENDED') {
        setError('You can only review completed sessions')
      }
      
      // Check if helper data is present
      const helperId = data.session.helperId || data.session.helper?.id
      if (!helperId) {
        console.error('Helper data missing from session:', data.session)
        setError('Session data is incomplete. Helper information is missing.')
      }
    } catch (err) {
      console.error('Failed to load session', err)
      setError('Failed to load session details: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (rating === 0) {
      setError('Please select a rating')
      return
    }

    // Get helperId from either session.helperId or session.helper.id
    const helperId = session.helperId || session.helper?.id
    
    if (!helperId) {
      setError('Session data is incomplete. Helper information is missing.')
      return
    }

    setSubmitting(true)
    setError('')

    try {
      await reviewService.submitReview({
        sessionId: sessionId,
        userId: user.id,
        helperId: helperId,
        rating: rating,
        reviewText: reviewText
      })
      
      alert('Review submitted successfully!')
      navigate('/user/sessions')
    } catch (err) {
      console.error('Submit review error:', err)
      setError(err.response?.data?.message || 'Failed to submit review')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <><Navbar /><div className="loading">Loading...</div></>

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Submit Review</h1>
        
        {session && (
          <div className="card">
            <h3>Session Details</h3>
            <p><strong>Session ID:</strong> {session.id}</p>
            <p><strong>Helper:</strong> {session.helperName || session.helper?.user?.firstName || 'Unknown'}</p>
            <p><strong>Category:</strong> {session.categoryName || session.category?.name || 'General'}</p>
            <p><strong>Date:</strong> {session.startedAt ? new Date(session.startedAt).toLocaleString() : 'N/A'}</p>
          </div>
        )}

        <div className="card">
          <h3>Your Review</h3>
          
          {error && (
            <div style={{ 
              padding: '10px', 
              backgroundColor: '#f8d7da', 
              color: '#721c24', 
              borderRadius: '4px',
              marginBottom: '15px'
            }}>
              {error}
            </div>
          )}

          {session?.status === 'ENDED' && (
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Rating (1-5 stars)</label>
                <div style={{ display: 'flex', gap: '10px', fontSize: '32px', marginTop: '10px' }}>
                  {[1, 2, 3, 4, 5].map(star => (
                    <span
                      key={star}
                      onClick={() => setRating(star)}
                      style={{ 
                        cursor: 'pointer',
                        color: star <= rating ? '#ffc107' : '#ddd',
                        transition: 'color 0.2s'
                      }}
                    >
                      ★
                    </span>
                  ))}
                </div>
                <p style={{ marginTop: '5px', fontSize: '14px', color: '#666' }}>
                  {rating === 0 && 'Click to rate'}
                  {rating === 1 && '1 star - Poor'}
                  {rating === 2 && '2 stars - Fair'}
                  {rating === 3 && '3 stars - Good'}
                  {rating === 4 && '4 stars - Very Good'}
                  {rating === 5 && '5 stars - Excellent'}
                </p>
              </div>

              <div className="form-group">
                <label>Review (optional)</label>
                <textarea
                  value={reviewText}
                  onChange={(e) => setReviewText(e.target.value)}
                  placeholder="Share your experience with this helper..."
                  rows="5"
                  style={{ 
                    width: '100%', 
                    padding: '10px', 
                    border: '1px solid #ddd', 
                    borderRadius: '4px',
                    fontFamily: 'inherit'
                  }}
                />
              </div>

              <div style={{ display: 'flex', gap: '10px' }}>
                <button 
                  type="submit" 
                  className="btn btn-primary"
                  disabled={submitting || rating === 0}
                >
                  {submitting ? 'Submitting...' : 'Submit Review'}
                </button>
                <button 
                  type="button" 
                  onClick={() => navigate('/user/sessions')}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </>
  )
}

export default SubmitReview
