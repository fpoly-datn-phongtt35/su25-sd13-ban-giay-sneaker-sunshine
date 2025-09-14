// src/utils/axiosInstance.ts
import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || 'http://localhost:8080',
  withCredentials: false,
})

// ✅ luôn lấy token mới nhất trước mỗi request
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('access_token') || localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  else delete config.headers.Authorization
  // (tuỳ chọn) chống cache GET
  if (config.method === 'get') {
    const url = new URL(config.url!, window.location.origin)
    url.searchParams.set('_ts', String(Date.now()))
    config.url = url.pathname + url.search
  }
  return config
})

export default apiClient
