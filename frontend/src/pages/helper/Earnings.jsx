import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { helperService } from '../../services/helperService'
import { billingService } from '../../services/billingService'
import Navbar from '../../components/Navbar'

const HelperEarnings = () => {
  const { user } = useAuth()
  const [helper, setHelper] = useState(null)
  const [earnings, setEarnings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const helperData = await helperService.getHelperByUserId(user.id)
      setHelper(helperData)
      const earningsData = await billingService.getHelperEarnings(helperData.id)
      setEarnings(earningsData)
    } catch (err) {
      console.error('Failed to load earnings', err)
    } finally {
      setLoading(false)
    }
  }

  const calculateNetEarning = (amount, platformFee) => {
    return parseFloat(amount || 0) - parseFloat(platformFee || 0)
  }

  const totalGross = earnings.reduce((sum, t) => sum + parseFloat(t.amount || 0), 0)
  const totalFees = earnings.reduce((sum, t) => sum + parseFloat(t.platformFee || 0), 0)
  const totalNet = totalGross - totalFees

  const formatDuration = (session) => {
    if (!session?.startedAt || !session?.endedAt) return 'N/A'
    const start = new Date(session.startedAt)
    const end = new Date(session.endedAt)
    const diffMs = end - start
    const minutes = Math.floor(diffMs / 60000)
    const seconds = Math.floor((diffMs % 60000) / 1000)
    
    if (minutes > 0) {
      return `${minutes} min ${seconds} sec`
    } else {
      return `${seconds} sec`
    }
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>My Earnings</h1>
        
        <div className="card">
          <h2>Earnings Summary</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginTop: '15px' }}>
            <div style={{ padding: '15px', backgroundColor: '#e8f5e9', borderRadius: '8px' }}>
              <p style={{ margin: 0, fontSize: '14px', color: '#666' }}>Total Gross</p>
              <p style={{ margin: '5px 0 0 0', fontSize: '28px', fontWeight: 'bold', color: '#2e7d32' }}>
                ${totalGross.toFixed(2)}
              </p>
            </div>
            <div style={{ padding: '15px', backgroundColor: '#fff3e0', borderRadius: '8px' }}>
              <p style={{ margin: 0, fontSize: '14px', color: '#666' }}>Platform Fees (10%)</p>
              <p style={{ margin: '5px 0 0 0', fontSize: '28px', fontWeight: 'bold', color: '#f57c00' }}>
                ${totalFees.toFixed(2)}
              </p>
            </div>
            <div style={{ padding: '15px', backgroundColor: '#e3f2fd', borderRadius: '8px' }}>
              <p style={{ margin: 0, fontSize: '14px', color: '#666' }}>Net Earnings</p>
              <p style={{ margin: '5px 0 0 0', fontSize: '28px', fontWeight: 'bold', color: '#1976d2' }}>
                ${totalNet.toFixed(2)}
              </p>
            </div>
          </div>
        </div>

        <div className="card">
          <h2>Transaction History</h2>
          {loading ? (
            <div className="loading">Loading...</div>
          ) : earnings.length === 0 ? (
            <p>No earnings yet</p>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid #ddd' }}>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Date</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Client</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Service</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Duration</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Rate</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Gross</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Fee</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Net</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Status</th>
                </tr>
              </thead>
              <tbody>
                {earnings.map(transaction => (
                  <tr key={transaction.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '10px' }}>
                      {transaction.processedAt ? new Date(transaction.processedAt).toLocaleDateString() : '-'}
                    </td>
                    <td style={{ padding: '10px' }}>
                      {transaction.session?.user?.firstName} {transaction.session?.user?.lastName}
                    </td>
                    <td style={{ padding: '10px' }}>
                      {transaction.session?.category?.name || 'General'}
                    </td>
                    <td style={{ padding: '10px' }}>
                      {formatDuration(transaction.session)}
                    </td>
                    <td style={{ padding: '10px' }}>
                      ${transaction.session?.helperRate ? parseFloat(transaction.session.helperRate).toFixed(2) : '-'}/hr
                    </td>
                    <td style={{ padding: '10px' }}>
                      ${parseFloat(transaction.amount).toFixed(2)}
                    </td>
                    <td style={{ padding: '10px', color: '#f57c00' }}>
                      -${parseFloat(transaction.platformFee || 0).toFixed(2)}
                    </td>
                    <td style={{ padding: '10px', fontWeight: 'bold', color: '#2e7d32' }}>
                      ${calculateNetEarning(transaction.amount, transaction.platformFee).toFixed(2)}
                    </td>
                    <td style={{ padding: '10px' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '4px',
                        backgroundColor: transaction.status === 'SUCCEEDED' ? '#28a745' : '#ffc107',
                        color: 'white',
                        fontSize: '12px'
                      }}>
                        {transaction.status}
                      </span>
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

export default HelperEarnings
