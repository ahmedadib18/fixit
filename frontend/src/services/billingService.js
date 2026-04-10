import api from './api'

export const billingService = {
  getUserTransactions: async (userId) => {
    const response = await api.get(`/billing/transactions/${userId}`)
    return response.data
  },

  processBilling: async (billingData) => {
    const response = await api.post('/billing/process', billingData)
    return response.data
  },

  getReceipt: async (transactionId) => {
    const response = await api.get(`/billing/receipts/${transactionId}`)
    return response.data
  },

  downloadReceipt: async (transactionId) => {
    const response = await api.get(`/billing/receipts/${transactionId}/download`, {
      responseType: 'blob'
    })
    return response.data
  },

  getHelperEarnings: async (helperId) => {
    const response = await api.get(`/billing/earnings/${helperId}`)
    return response.data
  },

  getEarningDetail: async (transactionId) => {
    const response = await api.get(`/billing/earnings/${transactionId}/detail`)
    return response.data
  },

  downloadEarningsStatement: async (helperId) => {
    const response = await api.get(`/billing/earnings/${helperId}/statement`, {
      responseType: 'blob'
    })
    return response.data
  }
}
