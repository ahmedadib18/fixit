import api from './api'

export const reviewService = {
  submitReview: async (sessionId, reviewData) => {
    const response = await api.post(`/reviews/sessions/${sessionId}`, reviewData)
    return response.data
  },

  getHelperReviews: async (helperId) => {
    const response = await api.get(`/reviews/helpers/${helperId}`)
    return response.data
  },

  getHelperAverageRating: async (helperId) => {
    const response = await api.get(`/reviews/helpers/${helperId}/rating`)
    return response.data
  }
}
