import api from './api'

export const supportService = {
  createTicket: async (ticketData) => {
    const response = await api.post('/tickets', ticketData)
    return response.data
  },

  getUserTickets: async (userId) => {
    const response = await api.get(`/tickets/user/${userId}`)
    return response.data
  },

  getTicketDetail: async (ticketId) => {
    const response = await api.get(`/tickets/${ticketId}`)
    return response.data
  }
}
