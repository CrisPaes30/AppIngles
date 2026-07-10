import api, { unwrap } from '@/services/api'
import type { AuthResponse } from '@/types/auth'
import type { ApiResponse } from '@/types/api'

export const authService = {
  async loginWithGoogle(idToken: string): Promise<AuthResponse> {
    const res = await api.post<ApiResponse<AuthResponse>>('/auth/google', { idToken })
    return unwrap(res)
  },
}
