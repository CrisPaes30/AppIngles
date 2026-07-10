import { useMutation, useQueryClient } from '@tanstack/react-query'
import { exerciseService } from '@/services/exercise.service'
import type { GenerateExerciseRequest, AnswerExerciseRequest } from '@/types/exercise'

export function useGenerateExercise() {
  return useMutation({
    mutationFn: (data: GenerateExerciseRequest) => exerciseService.generate(data),
  })
}

export function useAnswerExercise() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: AnswerExerciseRequest }) =>
      exerciseService.answer(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['dashboard'] })
      qc.invalidateQueries({ queryKey: ['vocabulary'] })
    },
  })
}

export function useAnalyzeSentence() {
  return useMutation({
    mutationFn: ({ sentence, wordId }: { sentence: string; wordId?: number }) =>
      exerciseService.analyzeSentence(sentence, wordId),
  })
}
