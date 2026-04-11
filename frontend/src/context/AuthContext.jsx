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
    const data = await authService.login(credentials)
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
    
    setToken(null)
    setUser(null)
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const cleanupMediaResources = () => {
    // Stop all media streams (camera/microphone)
    navigator.mediaDevices.getUserMedia({ video: true, audio: true })
      .then(stream => {
        stream.getTracks().forEach(track => {
          track.stop()
          console.log('Stopped media track:', track.kind)
        })
      })
      .catch(() => {
        // Ignore errors if no stream exists
      })

    // Get all video elements and clear their sources
    const videoElements = document.querySelectorAll('video')
    videoElements.forEach(video => {
      if (video.srcObject) {
        const stream = video.srcObject
        stream.getTracks().forEach(track => track.stop())
        video.srcObject = null
      }
    })

    console.log('Media resources cleaned up on logout')
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
