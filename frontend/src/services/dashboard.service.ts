import api, { unwrap } from './api'
import type { ApiResponse } from '@/types/api'
import type { DashboardData } from '@/types/dashboard'

export const dashboardService = {
  async get(): Promise<DashboardData> {
    const res = await api.get<ApiResponse<DashboardData>>('/dashboard')
    return unwrap(res)
  },
}
