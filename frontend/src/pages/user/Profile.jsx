import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { userService } from '../../services/userService'
import { authService } from '../../services/authService'
import Navbar from '../../components/Navbar'

const UserProfile = () => {
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [countries, setCountries] = useState([])
  const [cities, setCities] = useState([])
  const [selectedCountry, setSelectedCountry] = useState('')
  const [editing, setEditing] = useState(false)
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    cityId: ''
  })
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadProfile()
    loadCountries()
  }, [])

  const loadProfile = async () => {
    try {
      console.log('Loading profile for user ID:', user.id)
      const data = await userService.getProfile(user.id)
      console.log('Profile data received:', data)
      setProfile(data)
      setFormData({
        firstName: data.firstName || '',
        lastName: data.lastName || '',
        email: data.email || '',
        phone: data.phone || '',
        cityId: data.cityId || ''
      })
      if (data.cityId) {
        const city = await authService.getCities(data.cityId)
        if (city.length > 0) {
          setSelectedCountry(city[0].countryId)
          setCities(city)
        }
      }
    } catch (err) {
      console.error('Failed to load profile:', err)
      console.error('Error details:', err.response?.data)
      setMessage('Failed to load profile: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const loadCountries = async () => {
    try {
      const data = await authService.getCountries()
      setCountries(data)
    } catch (err) {
      console.error('Failed to load countries', err)
    }
  }

  const handleCountryChange = async (countryId) => {
    setSelectedCountry(countryId)
    if (countryId) {
      try {
        const data = await authService.getCities(countryId)
        setCities(data)
      } catch (err) {
        console.error('Failed to load cities', err)
      }
    }
  }

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setMessage('')
    try {
      await userService.updateProfile(user.id, formData)
      setMessage('Profile updated successfully')
      setEditing(false)
      loadProfile()
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
          <h2>My Profile</h2>
          {!editing ? (
            <>
              <div style={{ marginTop: '20px' }}>
                <p><strong>Name:</strong> {profile.firstName} {profile.lastName}</p>
                <p><strong>Email:</strong> {profile.email}</p>
                <p><strong>Phone:</strong> {profile.phone || 'Not provided'}</p>
                <p><strong>Account Status:</strong> {profile.accountStatus}</p>
              </div>
              <button onClick={() => setEditing(true)} className="btn btn-primary" style={{ marginTop: '20px' }}>
                Edit Profile
              </button>
            </>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>First Name</label>
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Last Name</label>
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label>Phone</label>
                <input
                  type="tel"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label>Country</label>
                <select value={selectedCountry} onChange={(e) => handleCountryChange(e.target.value)}>
                  <option value="">Select Country</option>
                  {countries.map(country => (
                    <option key={country.id} value={country.id}>{country.name}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>City</label>
                <select name="cityId" value={formData.cityId} onChange={handleChange}>
                  <option value="">Select City</option>
                  {cities.map(city => (
                    <option key={city.id} value={city.id}>{city.name}</option>
                  ))}
                </select>
              </div>
              {message && <div className={message.includes('success') ? 'success' : 'error'}>{message}</div>}
              <div style={{ display: 'flex', gap: '10px' }}>
                <button type="submit" className="btn btn-primary">Save Changes</button>
                <button type="button" onClick={() => setEditing(false)} className="btn btn-secondary">Cancel</button>
              </div>
            </form>
          )}
        </div>
      </div>
    </>
  )
}

export default UserProfile
