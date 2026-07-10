import axios from 'axios'
import type { ApiResponse } from '@/types/api'

const TOKEN_KEY = 'em_token'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthEndpoint = (error.config?.url as string | undefined)?.startsWith('/auth/')
    if (!isAuthEndpoint && (error.response?.status === 401 || error.response?.status === 403)) {
      localStorage.removeItem(TOKEN_KEY)
      window.location.href = '/login'
    }
    const apiMessage =
      error.response?.data?.message ??
      error.response?.data?.detail ??
      error.response?.data?.errors?.[0]

    const rejected = new Error(apiMessage ?? 'Erro inesperado. Tente novamente.') as Error & {
      diagnostic?: string
    }

    if (apiMessage == null) {
      const requestSentNoResponse = !!error.request && !error.response
      rejected.diagnostic = [
        `errorCode=${error.code ?? 'unknown'}`,
        `axiosMessage=${error.message ?? 'unknown'}`,
        `httpStatus=${error.response?.status ?? 'none'}`,
        `requestUrl=${error.config?.url ?? 'unknown'}`,
        `baseURL=${error.config?.baseURL ?? 'unknown'}`,
        `requestSentNoResponse(possible CORS/network)=${requestSentNoResponse}`,
      ].join(' | ')
    }

    return Promise.reject(rejected)
  },
)

export function unwrap<T>(response: { data: ApiResponse<T> }): T {
  return response.data.data
}

export default api
