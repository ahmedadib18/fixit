import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { helperService } from '../../services/helperService'
import Navbar from '../../components/Navbar'

const HelperDashboard = () => {
  const { user } = useAuth()
  const [helper, setHelper] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadHelper()
  }, [])

  const loadHelper = async () => {
    try {
      const data = await helperService.getHelperByUserId(user.id)
      setHelper(data)
    } catch (err) {
      console.error('Failed to load helper data', err)
    } finally {
      setLoading(false)
    }
  }

  const toggleAvailability = async () => {
    try {
      const updated = await helperService.updateAvailability(helper.id, !helper.isAvailable)
      setHelper(updated)
    } catch (err) {
      alert('Failed to update availability')
    }
  }

  if (loading) return <><Navbar /><div className="loading">Loading...</div></>

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Helper Dashboard</h1>
        
        <div className="card">
          <h2>Availability Status</h2>
          <p>You are currently: <strong>{helper?.isAvailable ? 'Available' : 'Unavailable'}</strong></p>
          <button onClick={toggleAvailability} className="btn btn-primary">
            {helper?.isAvailable ? 'Set Unavailable' : 'Set Available'}
          </button>
        </div>

        <div className="card">
          <h2>Quick Actions</h2>
          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <Link to="/helper/profile" className="btn btn-primary">Manage Profile</Link>
            <Link to="/helper/earnings" className="btn btn-secondary">View Earnings</Link>
          </div>
        </div>

        <div className="card">
          <h2>Profile Summary</h2>
          <p><strong>Headline:</strong> {helper?.professionalHeadline || 'Not set'}</p>
          <p><strong>Languages:</strong> {helper?.languagesSpoken || 'Not set'}</p>
          <p><strong>Specializations:</strong> {helper?.helperCategories?.length || 0}</p>
        </div>
      </div>
    </>
  )
}

export default HelperDashboard
