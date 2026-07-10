import api, { unwrap } from './api'
import type { ApiResponse } from '@/types/api'
import type { Category } from '@/types/vocabulary'

const BASE = '/categories'

export interface CreateCategoryRequest {
  name: string
  description?: string
  color: string
  icon: string
}

export const categoryService = {
  async list(): Promise<Category[]> {
    const res = await api.get<ApiResponse<Category[]>>(BASE)
    return unwrap(res)
  },

  async create(data: CreateCategoryRequest): Promise<Category> {
    const res = await api.post<ApiResponse<Category>>(BASE, data)
    return unwrap(res)
  },

  async update(id: number, data: Partial<CreateCategoryRequest>): Promise<Category> {
    const res = await api.put<ApiResponse<Category>>(`${BASE}/${id}`, data)
    return unwrap(res)
  },

  async remove(id: number): Promise<void> {
    await api.delete(`${BASE}/${id}`)
  },
}
