import api from './api'

export const authService = {
  register: async (userData) => {
    const response = await api.post('/auth/register', userData)
    return response.data
  },

  login: async (credentials) => {
    const response = await api.post('/auth/login', credentials)
    return response.data
  },

  loginWithGoogle: async (accessToken, userType) => {
    const response = await api.post('/auth/google', { accessToken, userType })
    return response.data
  },

  getCountries: async () => {
    const response = await api.get('/auth/countries')
    return response.data
  },

  getCities: async (countryId) => {
    const response = await api.get(`/auth/countries/${countryId}/cities`)
    return response.data
  }
}
