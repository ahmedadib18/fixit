import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { helperService } from '../../services/helperService'
import { authService } from '../../services/authService'
import Navbar from '../../components/Navbar'

const HelperProfile = () => {
  const { user } = useAuth()
  const [helper, setHelper] = useState(null)
  const [categories, setCategories] = useState([])
  const [editing, setEditing] = useState(false)
  const [editingSpecializations, setEditingSpecializations] = useState(false)
  const [formData, setFormData] = useState({
    professionalHeadline: '',
    languagesSpoken: ''
  })
  const [specializationForm, setSpecializationForm] = useState({
    id: null,
    categoryId: '',
    hourlyRate: '',
    yearsExperience: ''
  })
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadHelper()
    loadCategories()
  }, [])

  const loadCategories = async () => {
    try {
      const categoriesData = [
        { id: 1, name: 'Computer & IT Support' },
        { id: 2, name: 'Home Appliance Repair' },
        { id: 3, name: 'Plumbing' },
        { id: 4, name: 'Electrical Work' },
        { id: 5, name: 'Automotive Repair' },
        { id: 6, name: 'Smartphone & Tablet Support' },
        { id: 7, name: 'Home Improvement' },
        { id: 8, name: 'Gardening & Landscaping' },
        { id: 9, name: 'Tutoring & Education' },
        { id: 10, name: 'Health & Fitness Coaching' }
      ]
      setCategories(categoriesData)
    } catch (err) {
      console.error('Failed to load categories', err)
    }
  }

  const loadHelper = async () => {
    try {
      const data = await helperService.getHelperByUserId(user.id)
      setHelper(data)
      setFormData({
        professionalHeadline: data.professionalHeadline || '',
        languagesSpoken: data.languagesSpoken || ''
      })
    } catch (err) {
      console.error('Failed to load helper profile:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSpecializationChange = (e) => {
    setSpecializationForm({ ...specializationForm, [e.target.name]: e.target.value })
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

  const handleEditSpecialization = (spec) => {
    setSpecializationForm({
      id: spec.id,
      categoryId: spec.categoryId,
      hourlyRate: spec.hourlyRate,
      yearsExperience: spec.yearsExperience
    })
    setEditingSpecializations(true)
  }

  const handleAddNewSpecialization = () => {
    setSpecializationForm({
      id: null,
      categoryId: '',
      hourlyRate: '',
      yearsExperience: ''
    })
    setEditingSpecializations(true)
  }

  const handleSaveSpecialization = async (e) => {
    e.preventDefault()
    setMessage('')
    try {
      const payload = {
        categoryIds: [parseInt(specializationForm.categoryId)],
        hourlyRates: [parseFloat(specializationForm.hourlyRate)],
        yearsExperiences: [parseInt(specializationForm.yearsExperience)]
      }
      await helperService.updateProfile(helper.id, payload)
      setMessage(specializationForm.id ? 'Specialization updated successfully' : 'Specialization added successfully')
      setEditingSpecializations(false)
      setSpecializationForm({ id: null, categoryId: '', hourlyRate: '', yearsExperience: '' })
      loadHelper()
    } catch (err) {
      setMessage(err.response?.data?.message || 'Failed to save specialization')
    }
  }

  const handleRemoveSpecialization = async (helperCategoryId) => {
    if (!window.confirm('Are you sure you want to remove this specialization?')) return
    
    setMessage('')
    try {
      await helperService.deleteSpecialization(helperCategoryId)
      setMessage('Specialization removed successfully')
      await loadHelper()
    } catch (err) {
      console.error('Remove specialization error:', err)
      setMessage(err.response?.data?.message || 'Failed to remove specialization')
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
          <h2>Specializations & Rates</h2>
          {helper.specializations && helper.specializations.length > 0 ? (
            <div>
              {helper.specializations.map(spec => (
                <div key={spec.id} style={{ padding: '10px', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <p><strong>{spec.categoryName}</strong></p>
                    <p>Hourly Rate: ${spec.hourlyRate}/hr</p>
                    <p>Experience: {spec.yearsExperience} years</p>
                  </div>
                  <div style={{ display: 'flex', gap: '10px' }}>
                    <button 
                      onClick={() => handleEditSpecialization(spec)} 
                      className="btn btn-secondary"
                      style={{ padding: '5px 15px' }}
                    >
                      Edit
                    </button>
                    <button 
                      onClick={() => handleRemoveSpecialization(spec.id)} 
                      className="btn btn-danger"
                      style={{ padding: '5px 15px' }}
                    >
                      Remove
                    </button>
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p>No specializations added yet. Add your services and rates below.</p>
          )}
          
          {!editingSpecializations ? (
            <button 
              onClick={handleAddNewSpecialization} 
              className="btn btn-primary" 
              style={{ marginTop: '20px' }}
            >
              Add Specialization
            </button>
          ) : (
            <form onSubmit={handleSaveSpecialization} style={{ marginTop: '20px' }}>
              <div className="form-group">
                <label>Service Category</label>
                <select
                  name="categoryId"
                  value={specializationForm.categoryId}
                  onChange={handleSpecializationChange}
                  required
                >
                  <option value="">Select a category</option>
                  {categories.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Hourly Rate ($)</label>
                <input
                  type="number"
                  name="hourlyRate"
                  value={specializationForm.hourlyRate}
                  onChange={handleSpecializationChange}
                  placeholder="e.g., 50.00"
                  step="0.01"
                  min="0"
                  required
                />
              </div>
              <div className="form-group">
                <label>Years of Experience</label>
                <input
                  type="number"
                  name="yearsExperience"
                  value={specializationForm.yearsExperience}
                  onChange={handleSpecializationChange}
                  placeholder="e.g., 5"
                  min="0"
                  required
                />
              </div>
              {message && <div className={message.includes('success') ? 'success' : 'error'}>{message}</div>}
              <div style={{ display: 'flex', gap: '10px' }}>
                <button type="submit" className="btn btn-primary">
                  {specializationForm.id ? 'Update Specialization' : 'Add Specialization'}
                </button>
                <button 
                  type="button" 
                  onClick={() => {
                    setEditingSpecializations(false)
                    setSpecializationForm({ id: null, categoryId: '', hourlyRate: '', yearsExperience: '' })
                  }} 
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

export default HelperProfile
