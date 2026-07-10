import api, { unwrap } from './api'
import type { ApiResponse, PageResponse } from '@/types/api'
import type {
  VocabularyWord,
  VocabularySummary,
  WordDetails,
  CreateVocabularyRequest,
  UpdateVocabularyRequest,
} from '@/types/vocabulary'

const BASE = '/vocabulary'

export const vocabularyService = {
  async list(page = 0, size = 20, search?: string): Promise<PageResponse<VocabularySummary>> {
    const params: Record<string, unknown> = { page, size }
    if (search) params.search = search
    const res = await api.get<ApiResponse<PageResponse<VocabularySummary>>>(BASE, { params })
    return unwrap(res)
  },

  async get(id: number): Promise<VocabularyWord> {
    const res = await api.get<ApiResponse<VocabularyWord>>(`${BASE}/${id}`)
    return unwrap(res)
  },

  async create(data: CreateVocabularyRequest): Promise<VocabularyWord> {
    const res = await api.post<ApiResponse<VocabularyWord>>(BASE, data)
    return unwrap(res)
  },

  async update(id: number, data: UpdateVocabularyRequest): Promise<VocabularyWord> {
    const res = await api.put<ApiResponse<VocabularyWord>>(`${BASE}/${id}`, data)
    return unwrap(res)
  },

  async remove(id: number): Promise<void> {
    await api.delete(`${BASE}/${id}`)
  },

  async listDueToday(): Promise<VocabularySummary[]> {
    const res = await api.get<ApiResponse<VocabularySummary[]>>(`${BASE}/due-today`)
    return unwrap(res)
  },

  async listWeak(): Promise<VocabularySummary[]> {
    const res = await api.get<ApiResponse<VocabularySummary[]>>(`${BASE}/weak`)
    return unwrap(res)
  },

  async enrich(word: string): Promise<WordDetails> {
    const res = await api.post<ApiResponse<WordDetails>>(`${BASE}/enrich`, { word })
    return unwrap(res)
  },
}
