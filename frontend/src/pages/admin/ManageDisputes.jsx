import { useState, useEffect } from 'react'
import { adminService } from '../../services/adminService'
import Navbar from '../../components/Navbar'

const ManageDisputes = () => {
  const [disputes, setDisputes] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadDisputes()
  }, [])

  const loadDisputes = async () => {
    try {
      const data = await adminService.getAllDisputes()
      setDisputes(data)
    } catch (err) {
      console.error('Failed to load disputes', err)
    } finally {
      setLoading(false)
    }
  }

  const handleResolve = async (disputeId) => {
    const resolution = prompt('Enter resolution:')
    if (!resolution) return
    
    const refundAmount = prompt('Enter refund amount (or 0 for no refund):')
    if (refundAmount === null) return

    try {
      await adminService.resolveDispute(disputeId, resolution, parseFloat(refundAmount))
      loadDisputes()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to resolve dispute')
    }
  }

  const handleDismiss = async (disputeId) => {
    if (!confirm('Are you sure you want to dismiss this dispute?')) return

    try {
      await adminService.dismissDispute(disputeId)
      loadDisputes()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to dismiss dispute')
    }
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Manage Disputes</h1>
        <div className="card">
          {loading ? (
            <div className="loading">Loading...</div>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                  <th style={{ padding: '10px', textAlign: 'left' }}>ID</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Type</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Amount</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Status</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Created</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {disputes.map(dispute => (
                  <tr key={dispute.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '10px' }}>{dispute.id}</td>
                    <td style={{ padding: '10px' }}>{dispute.disputeType}</td>
                    <td style={{ padding: '10px' }}>${parseFloat(dispute.amount || 0).toFixed(2)}</td>
                    <td style={{ padding: '10px' }}>{dispute.status}</td>
                    <td style={{ padding: '10px' }}>
                      {new Date(dispute.createdAt).toLocaleDateString()}
                    </td>
                    <td style={{ padding: '10px' }}>
                      {dispute.status !== 'RESOLVED' && (
                        <>
                          <button onClick={() => handleResolve(dispute.id)} className="btn btn-success" style={{ padding: '5px 10px', marginRight: '5px' }}>
                            Resolve
                          </button>
                          <button onClick={() => handleDismiss(dispute.id)} className="btn btn-danger" style={{ padding: '5px 10px' }}>
                            Dismiss
                          </button>
                        </>
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

export default ManageDisputes
