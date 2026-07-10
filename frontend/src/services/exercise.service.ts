import api, { unwrap } from './api'
import type { ApiResponse } from '@/types/api'
import type {
  Exercise,
  GenerateExerciseRequest,
  AnswerExerciseRequest,
  ExerciseAnswer,
  SentencePractice,
} from '@/types/exercise'

const BASE = '/exercise'

export const exerciseService = {
  async generate(data: GenerateExerciseRequest = {}): Promise<Exercise> {
    const res = await api.post<ApiResponse<Exercise>>(`${BASE}/generate`, data)
    return unwrap(res)
  },

  async answer(id: number, data: AnswerExerciseRequest): Promise<ExerciseAnswer> {
    const res = await api.post<ApiResponse<ExerciseAnswer>>(`${BASE}/${id}/answer`, data)
    return unwrap(res)
  },

  async analyzeSentence(sentence: string, wordId?: number): Promise<SentencePractice> {
    const res = await api.post<ApiResponse<SentencePractice>>('/sentence/analyze', {
      sentence,
      vocabularyWordId: wordId,
    })
    return unwrap(res)
  },
}
