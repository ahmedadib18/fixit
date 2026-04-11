import { createContext, useState, useContext, useEffect } from 'react'
import { authService } from '../services/authService'

const AuthContext = createContext(null)

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken && storedUser) {
      setToken(storedToken)
      setUser(JSON.parse(storedUser))
    }
    setLoading(false)
  }, [])

  const login = async (credentials) => {
    console.log('🔐 Login attempt:', { email: credentials.email })
    
    // Clear any existing auth data before login
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    
    const data = await authService.login(credentials)
    console.log('✅ Login successful:', { userType: data.userType })
    
    setToken(data.token)
    setUser({
      id: data.userId,
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
      userType: data.userType
    })
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({
      id: data.userId,
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
      userType: data.userType
    }))
    return data
  }

  const register = async (userData) => {
    const data = await authService.register(userData)
    setToken(data.token)
    setUser({
      id: data.userId,
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
      userType: data.userType
    })
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({
      id: data.userId,
      email: data.email,
      firstName: data.firstName,
      lastName: data.lastName,
      userType: data.userType
    }))
    return data
  }

  const logout = () => {
    // Clean up WebRTC and media resources
    cleanupMediaResources()
    
    // Clear state
    setToken(null)
    setUser(null)
    
    // Clear localStorage completely
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    
    // Clear any other cached data
    sessionStorage.clear()
    
    console.log('Logout complete - all data cleared')
  }

  const cleanupMediaResources = () => {
    try {
      // Get all video elements and clear their sources
      const videoElements = document.querySelectorAll('video')
      videoElements.forEach(video => {
        if (video.srcObject) {
          const stream = video.srcObject
          stream.getTracks().forEach(track => {
            track.stop()
            console.log('Stopped media track:', track.kind)
          })
          video.srcObject = null
        }
      })

      console.log('Media resources cleaned up on logout')
    } catch (error) {
      console.log('Error cleaning up media resources:', error)
    }
  }

  const value = {
    user,
    token,
    login,
    register,
    logout,
    loading,
    isAuthenticated: !!token
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
