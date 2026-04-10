import api from './api'

export const searchService = {
  searchHelpers: async (filters) => {
    const params = new URLSearchParams()
    Object.keys(filters).forEach(key => {
      if (filters[key] !== null && filters[key] !== undefined && filters[key] !== '') {
        params.append(key, filters[key])
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
