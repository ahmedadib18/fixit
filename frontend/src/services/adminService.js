import api from './api'

export const adminService = {
  getAllUsers: async () => {
    const response = await api.get('/admin/users')
    return response.data
  },

  updateUserStatus: async (userId, action, reason) => {
    const response = await api.put(`/admin/users/${userId}/status`, { action, reason })
    return response.data
  },

  getAllDisputes: async () => {
    const response = await api.get('/admin/disputes')
    return response.data
  },

  resolveDispute: async (disputeId, resolution, refundAmount) => {
    const response = await api.put(`/admin/disputes/${disputeId}/resolve`, { resolution, refundAmount })
    return response.data
  },

  dismissDispute: async (disputeId) => {
    const response = await api.put(`/admin/disputes/${disputeId}/dismiss`)
    return response.data
  },

  getAllTickets: async () => {
    const response = await api.get('/admin/tickets')
    return response.data
  },

  addTicketResponse: async (ticketId, responderId, responseText) => {
    const response = await api.post(`/admin/tickets/${ticketId}/response`, { responderId, responseText })
    return response.data
  },

  closeTicket: async (ticketId) => {
    const response = await api.put(`/admin/tickets/${ticketId}/close`)
    return response.data
  },

  escalateTicket: async (ticketId) => {
    const response = await api.put(`/admin/tickets/${ticketId}/escalate`)
    return response.data
  }
}
