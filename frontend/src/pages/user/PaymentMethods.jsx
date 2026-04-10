import { useState, useEffect } from 'react'
import { useAuth } from '../../context/AuthContext'
import { userService } from '../../services/userService'
import Navbar from '../../components/Navbar'

const PaymentMethods = () => {
  const { user } = useAuth()
  const [paymentMethods, setPaymentMethods] = useState([])
  const [loading, setLoading] = useState(true)
  const [adding, setAdding] = useState(false)
  const [formData, setFormData] = useState({
    stripePaymentMethodId: '',
    cardLastFour: '',
    cardBrand: ''
  })

  useEffect(() => {
    loadPaymentMethods()
  }, [])

  const loadPaymentMethods = async () => {
    try {
      const data = await userService.getPaymentMethods(user.id)
      setPaymentMethods(data)
    } catch (err) {
      console.error('Failed to load payment methods', err)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await userService.addPaymentMethod(user.id, formData)
      setAdding(false)
      setFormData({ stripePaymentMethodId: '', cardLastFour: '', cardBrand: '' })
      loadPaymentMethods()
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to add payment method')
    }
  }

  const handleDelete = async (paymentMethodId) => {
    if (confirm('Are you sure you want to delete this payment method?')) {
      try {
        await userService.deletePaymentMethod(user.id, paymentMethodId)
        loadPaymentMethods()
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to delete payment method')
      }
    }
  }

  return (
    <>
      <Navbar />
      <div className="container">
        <h1>Payment Methods</h1>
        
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2>Your Cards</h2>
            <button onClick={() => setAdding(!adding)} className="btn btn-primary">
              {adding ? 'Cancel' : 'Add Payment Method'}
            </button>
          </div>

          {adding && (
            <form onSubmit={handleSubmit} style={{ marginTop: '20px', padding: '20px', background: '#f9f9f9', borderRadius: '4px' }}>
              <div className="form-group">
                <label>Stripe Payment Method ID</label>
                <input
                  type="text"
                  value={formData.stripePaymentMethodId}
                  onChange={(e) => setFormData({ ...formData, stripePaymentMethodId: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Card Last Four Digits</label>
                <input
                  type="text"
                  maxLength="4"
                  value={formData.cardLastFour}
                  onChange={(e) => setFormData({ ...formData, cardLastFour: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Card Brand</label>
                <input
                  type="text"
                  value={formData.cardBrand}
                  onChange={(e) => setFormData({ ...formData, cardBrand: e.target.value })}
                  placeholder="e.g., Visa, Mastercard"
                  required
                />
              </div>
              <button type="submit" className="btn btn-success">Add Card</button>
            </form>
          )}

          {loading ? (
            <div className="loading">Loading...</div>
          ) : paymentMethods.length === 0 ? (
            <p style={{ marginTop: '20px' }}>No payment methods added yet.</p>
          ) : (
            <div style={{ marginTop: '20px' }}>
              {paymentMethods.map(pm => (
                <div key={pm.id} style={{ padding: '15px', border: '1px solid #ddd', borderRadius: '4px', marginBottom: '10px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <p><strong>{pm.cardBrand}</strong> ending in {pm.cardLastFour}</p>
                    {pm.isDefault && <span style={{ color: '#28a745', fontSize: '12px' }}>Default</span>}
                  </div>
                  <button onClick={() => handleDelete(pm.id)} className="btn btn-danger" style={{ padding: '5px 10px' }}>
                    Delete
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </>
  )
}

export default PaymentMethods
