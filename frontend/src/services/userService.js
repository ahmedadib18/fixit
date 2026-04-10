import api from './api'

export const userService = {
  getProfile: async (userId) => {
    const response = await api.get(`/users/${userId}/profile`)
    return response.data
  },

  updateProfile: async (userId, profileData) => {
    const response = await api.put(`/users/${userId}/profile`, profileData)
    return response.data
  },

  getSessions: async (userId) => {
    const response = await api.get(`/users/${userId}/sessions`)
    return response.data
  },

  getPaymentMethods: async (userId) => {
    const response = await api.get(`/users/${userId}/payment-methods`)
    return response.data
  },

  addPaymentMethod: async (userId, paymentData) => {
    const response = await api.post(`/users/${userId}/payment-methods`, paymentData)
    return response.data
  },

  deletePaymentMethod: async (userId, paymentMethodId) => {
    const response = await api.delete(`/users/${userId}/payment-methods/${paymentMethodId}`)
    return response.data
  },

  uploadProfileImage: async (userId, file) => {
    const formData = new FormData()
    formData.append('file', file)
    const response = await api.post(`/users/${userId}/profile-image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return response.data
  }
}
