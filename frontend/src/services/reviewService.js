import api from './api'

export const reviewService = {
  submitReview: async (reviewData) => {
    const { sessionId, ...requestBody } = reviewData
    const response = await api.post(`/reviews/sessions/${sessionId}`, requestBody)
    return response.data
  },

  getHelperReviews: async (helperId) => {
    const response = await api.get(`/reviews/helpers/${helperId}`)
    return response.data
  },

  getHelperAverageRating: async (helperId) => {
    const response = await api.get(`/reviews/helpers/${helperId}/rating`)
    return response.data
  },

  getReviewBySession: async (sessionId) => {
    const response = await api.get(`/reviews/sessions/${sessionId}`)
    return response.data
  },

  updateReviewVisibility: async (reviewId, isPublic) => {
    const response = await api.put(`/reviews/${reviewId}/visibility`, { isPublic })
    return response.data
  },

  deleteReview: async (reviewId) => {
    const response = await api.delete(`/reviews/${reviewId}`)
    return response.data
  }
}
