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

  const totalEarnings = earnings.reduce((sum, t) => sum + parseFloat(t.amount || 0), 0)

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>My Earnings</h1>
        
        <div className="card">
          <h2>Total Earnings</h2>
          <p style={{ fontSize: '32px', fontWeight: 'bold', color: '#28a745' }}>
            ${totalEarnings.toFixed(2)}
          </p>
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
                  <th style={{ padding: '10px', textAlign: 'left' }}>Transaction ID</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Session ID</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Amount</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Status</th>
                  <th style={{ padding: '10px', textAlign: 'left' }}>Date</th>
                </tr>
              </thead>
              <tbody>
                {earnings.map(transaction => (
                  <tr key={transaction.id} style={{ borderBottom: '1px solid #eee' }}>
                    <td style={{ padding: '10px' }}>{transaction.id}</td>
                    <td style={{ padding: '10px' }}>{transaction.sessionId}</td>
                    <td style={{ padding: '10px' }}>${parseFloat(transaction.amount).toFixed(2)}</td>
                    <td style={{ padding: '10px' }}>{transaction.status}</td>
                    <td style={{ padding: '10px' }}>
                      {transaction.processedAt ? new Date(transaction.processedAt).toLocaleDateString() : '-'}
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
