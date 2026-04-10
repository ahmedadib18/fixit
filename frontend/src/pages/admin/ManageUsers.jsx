import { useState, useEffect } from 'react'
import { adminService } from '../../services/adminService'
import Navbar from '../../components/Navbar'

const ManageUsers = () => {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadUsers()
  }, [])

  const loadUsers = async () => {
    try {
      const data = await adminService.getAllUsers()
      setUsers(data)
    } catch (err) {
      console.error('Failed to load users', err)
    } finally {
      setLoading(false)
    }
  }

  const handleStatusChange = async (userId, action) => {
    const reason = prompt(`Enter reason for ${action}:`)
    if (!reason) return

    try {
      await adminService.updateUserStatus(userId, action, reason)
      loadUsers()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update user status')
    }
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Manage Users</h1>
        <div className="card">
          {loading ? (
            <div className="loading">Loading...</div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                  <th style={{ padding: '10px', textAlign: 'left' }}>ID</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Name</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Email</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Type</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Status</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map(user => (
                  <tr key={user.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '10px' }}>{user.id}</td>
                    <td style={{ padding: '10px' }}>{user.firstName} {user.lastName}</td>
                    <td style={{ padding: '10px' }}>{user.email}</td>
                    <td style={{ padding: '10px' }}>{user.userType}</td>
                    <td style={{ padding: '10px' }}>{user.accountStatus}</td>
                    <td style={{ padding: '10px' }}>
                      <select onChange={(e) => handleStatusChange(user.id, e.target.value)} defaultValue="">
                        <option value="" disabled>Select Action</option>
                        <option value="SUSPEND">Suspend</option>
                        <option value="BAN">Ban</option>
                        <option value="REACTIVATE">Reactivate</option>
                      </select>
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

export default ManageUsers
