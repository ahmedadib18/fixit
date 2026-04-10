import api from './api'

export const searchService = {
  searchHelpers: async (filters) => {
    const params = new URLSearchParams()
    Object.keys(filters).forEach(key => {
      const value = filters[key]
      // Only add non-empty values, and for availableNow, only add if true
      if (value !== null && value !== undefined && value !== '') {
        if (key === 'availableNow' && value === false) {
          // Don't send availableNow=false, just omit it
          return
        }
        params.append(key, value)
      }
    })
    const response = await api.get(`/search/helpers?${params.toString()}`)
    return response.data
  },

  getHelperProfile: async (helperId) => {
    const response = await api.get(`/search/helpers/${helperId}`)
    return response.data
  }
}
