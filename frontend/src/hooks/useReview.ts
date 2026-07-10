import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { reviewService } from '@/services/review.service'
import type { ReviewAnswerRequest } from '@/types/review'

export function useReviewCards() {
  return useQuery({
    queryKey: ['review', 'due'],
    queryFn:  reviewService.getDueCards,
  })
}

export function useSubmitReview() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ wordId, data }: { wordId: number; data: ReviewAnswerRequest }) =>
      reviewService.submitAnswer(wordId, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['dashboard'] })
      qc.invalidateQueries({ queryKey: ['vocabulary'] })
    },
  })
}
