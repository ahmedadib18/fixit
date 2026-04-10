import api from './api'

export const disputeService = {
  fileDispute: async (sessionId, disputeData) => {
    const response = await api.post(`/disputes/sessions/${sessionId}`, disputeData)
    return response.data
  },

  getSessionDisputes: async (sessionId) => {
    const response = await api.get(`/disputes/session/${sessionId}`)
    return response.data
  },

  getUserDisputes: async (userId) => {
    const response = await api.get(`/disputes/user/${userId}`)
    return response.data
  }
}
