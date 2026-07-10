import api, { unwrap } from './api'
import type { ApiResponse } from '@/types/api'
import type { ReviewCard, ReviewAnswerRequest, ReviewResult } from '@/types/review'

const BASE = '/reviews'

export const reviewService = {
  async getDueCards(): Promise<ReviewCard[]> {
    const res = await api.get<ApiResponse<ReviewCard[]>>(`${BASE}/today`)
    return unwrap(res)
  },

  async submitAnswer(wordId: number, data: ReviewAnswerRequest): Promise<ReviewResult> {
    const res = await api.post<ApiResponse<ReviewResult>>(`${BASE}/${wordId}/answer`, data)
    return unwrap(res)
  },
}
