import api from './api'

export const sessionService = {
  createSession: async (sessionData) => {
    const response = await api.post('/sessions', sessionData)
    return response.data
  },

  acceptSession: async (sessionId) => {
    const response = await api.put(`/sessions/${sessionId}/accept`)
    return response.data
  },

  rejectSession: async (sessionId) => {
    const response = await api.put(`/sessions/${sessionId}/reject`)
    return response.data
  },

  endSession: async (sessionId) => {
    const response = await api.put(`/sessions/${sessionId}/end`)
    return response.data
  },

  cancelSession: async (sessionId) => {
    const response = await api.put(`/sessions/${sessionId}/cancel`)
    return response.data
  },

  sendMessage: async (sessionId, messageData) => {
    const response = await api.post(`/sessions/${sessionId}/messages`, messageData)
    return response.data
  },

  getSessionLog: async (sessionId) => {
    const response = await api.get(`/sessions/${sessionId}/log`)
    return response.data
  },

  updateConsent: async (sessionId, consentData) => {
    const response = await api.put(`/sessions/${sessionId}/consent`, consentData)
    return response.data
  },

  exportSessionLog: async (sessionId) => {
    const response = await api.get(`/sessions/${sessionId}/export`, {
      responseType: 'blob'
    })
    return response.data
  }
}
