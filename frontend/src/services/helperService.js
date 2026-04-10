import api from './api'

export const helperService = {
  getHelperByUserId: async (userId) => {
    const response = await api.get(`/helpers/by-user/${userId}`)
    return response.data
  },

  updateProfile: async (helperId, profileData) => {
    const response = await api.put(`/helpers/${helperId}/profile`, profileData)
    return response.data
  },

  updateAvailability: async (helperId, isAvailable) => {
    const response = await api.put(`/helpers/${helperId}/availability`, { isAvailable })
    return response.data
  },

  uploadCertificate: async (helperId, categoryId, file) => {
    const formData = new FormData()
    formData.append('file', file)
    const response = await api.post(`/helpers/${helperId}/categories/${categoryId}/certificate`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    return response.data
  },

  getHelperSessions: async (helperId) => {
    const response = await api.get(`/helpers/${helperId}/sessions`)
    return response.data
  }
}
