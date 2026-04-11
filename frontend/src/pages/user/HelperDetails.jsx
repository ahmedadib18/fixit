import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { searchService } from '../../services/searchService'
import { sessionService } from '../../services/sessionService'
import Navbar from '../../components/Navbar'

const HelperDetails = () => {
  const { helperId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [helperData, setHelperData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)

  useEffect(() => {
    loadHelperProfile()
  }, [helperId])

  const loadHelperProfile = async () => {
    try {
      const data = await searchService.getHelperProfile(helperId)
      setHelperData(data)
    } catch (err) {
      console.error('Failed to load helper profile', err)
    } finally {
      setLoading(false)
    }
  }

  const handleStartSession = async () => {
    setCreating(true)
    try {
      const sessionData = {
        userId: user.id,
        helperId: parseInt(helperId),
        categoryId: helperData.helper.helperCategories?.[0]?.categoryId || null
      }
      console.log('Creating session with data:', sessionData)
      const session = await sessionService.createSession(sessionData)
      console.log('Session created:', session)
      navigate(`/session/${session.id}`)
    } catch (err) {
      console.error('Session creation error:', err)
      alert(err.response?.data?.message || 'Failed to create session')
    } finally {
      setCreating(false)
    }
  }

  if (loading) return <><Navbar /><div className="loading">Loading...</div></>
  if (!helperData) return <><Navbar /><div className="container">Helper not found</div></>

  const { helper, averageRating, reviews } = helperData

  return (
    <>
      <Navbar />
      <div className="container">
        <div className="card">
          <h1>{helper.user?.firstName} {helper.user?.lastName}</h1>
          <p><strong>Professional Headline:</strong> {helper.professionalHeadline || 'No headline'}</p>
          <p><strong>Languages:</strong> {helper.languagesSpoken || 'Not specified'}</p>
          <p><strong>Available:</strong> {helper.isAvailable ? 'Yes' : 'No'}</p>
          <p><strong>Average Rating:</strong> {averageRating ? averageRating.toFixed(1) : 'No ratings yet'} ⭐</p>
          
          <button 
            onClick={handleStartSession} 
            className="btn btn-primary" 
            style={{ marginTop: '20px' }}
            disabled={!helper.isAvailable || creating}
          >
            {creating ? 'Starting Session...' : 'Start Session'}
          </button>
        </div>

        <div className="card">
          <h2>Specializations</h2>
          {helper.helperCategories && helper.helperCategories.length > 0 ? (
            <ul>
              {helper.helperCategories.map(hc => (
                <li key={hc.id}>
                  {hc.category?.name} - ${hc.hourlyRate}/hr - {hc.yearsExperience} years experience
                </li>
              ))}
            </ul>
          ) : (
            <p>No specializations listed</p>
          )}
        </div>

        <div className="card">
          <h2>Reviews ({reviews?.length || 0})</h2>
          {reviews && reviews.length > 0 ? (
            reviews.map(review => (
              <div key={review.id} style={{ borderBottom: '1px solid #eee', padding: '10px 0' }}>
                <p><strong>Rating:</strong> {review.rating} ⭐</p>
                <p>{review.reviewText}</p>
                <p style={{ fontSize: '12px', color: '#666' }}>
                  {new Date(review.createdAt).toLocaleDateString()}
                </p>
              </div>
            ))
          ) : (
            <p>No reviews yet</p>
          )}
        </div>
      </div>
    </>
  )
}

export default HelperDetails
