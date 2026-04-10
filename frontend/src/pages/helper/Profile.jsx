import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { helperService } from '../../services/helperService'
import Navbar from '../../components/Navbar'

const HelperProfile = () => {
  const { user } = useAuth()
  const [helper, setHelper] = useState(null)
  const [editing, setEditing] = useState(false)
  const [formData, setFormData] = useState({
    professionalHeadline: '',
    languagesSpoken: ''
  })
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadHelper()
  }, [])

  const loadHelper = async () => {
    try {
      const data = await helperService.getHelperByUserId(user.id)
      setHelper(data)
      setFormData({
        professionalHeadline: data.professionalHeadline || '',
        languagesSpoken: data.languagesSpoken || ''
      })
    } catch (err) {
      console.error('Failed to load helper profile', err)
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setMessage('')
    try {
      await helperService.updateProfile(helper.id, formData)
      setMessage('Profile updated successfully')
      setEditing(false)
      loadHelper()
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to update profile')
    }
  }

  if (loading) return <><Navbar /><div className="loading">Loading...</div></>

  return (
    <>
      <Navbar />
      <div className="container">
        <div className="card">
          <h2>Helper Profile</h2>
          {!editing ? (
            <>
              <div style={{ marginTop: '20px' }}>
                <p><strong>Professional Headline:</strong> {helper.professionalHeadline || 'Not set'}</p>
                <p><strong>Languages Spoken:</strong> {helper.languagesSpoken || 'Not set'}</p>
                <p><strong>Available:</strong> {helper.isAvailable ? 'Yes' : 'No'}</p>
              </div>
              <button onClick={() => setEditing(true)} className="btn btn-primary" style={{ marginTop: '20px' }}>
                Edit Profile
              </button>
            </>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Professional Headline</label>
                <input
                  type="text"
                  name="professionalHeadline"
                  value={formData.professionalHeadline}
                  onChange={handleChange}
                  placeholder="e.g., Expert IT Support Specialist"
                />
              </div>
              <div className="form-group">
                <label>Languages Spoken</label>
                <input
                  type="text"
                  name="languagesSpoken"
                  value={formData.languagesSpoken}
                  onChange={handleChange}
                  placeholder="e.g., English, Spanish, French"
                />
              </div>
              {message && <div className={message.includes('success') ? 'success' : 'error'}>{message}</div>}
              <div style={{ display: 'flex', gap: '10px' }}>
                <button type="submit" className="btn btn-primary">Save Changes</button>
                <button type="button" onClick={() => setEditing(false)} className="btn btn-secondary">Cancel</button>
              </div>
            </form>
          )}
        </div>

        <div className="card">
          <h2>Specializations</h2>
          {helper.helperCategories && helper.helperCategories.length > 0 ? (
            <ul>
              {helper.helperCategories.map(hc => (
                <li key={hc.id}>
                  {hc.category?.name} - ${hc.hourlyRate}/hr - {hc.yearsExperience} years
                </li>
              ))}
            </ul>
          ) : (
            <p>No specializations added yet</p>
          )}
        </div>
      </div>
    </>
  )
}

export default HelperProfile
