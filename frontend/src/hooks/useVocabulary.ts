import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { vocabularyService } from '@/services/vocabulary.service'
import { categoryService } from '@/services/category.service'
import type { CreateVocabularyRequest, UpdateVocabularyRequest } from '@/types/vocabulary'

export function useEnrichVocabulary() {
  return useMutation({
    mutationFn: (word: string) => vocabularyService.enrich(word),
  })
}

export const VOCAB_KEY = ['vocabulary'] as const

export function useVocabularyList(page: number, search: string) {
  return useQuery({
    queryKey: [...VOCAB_KEY, page, search],
    queryFn:  () => vocabularyService.list(page, 12, search || undefined),
  })
}

export function useVocabularyDetail(id: number | null) {
  return useQuery({
    queryKey: [...VOCAB_KEY, id],
    queryFn:  () => vocabularyService.get(id!),
    enabled:  id != null,
  })
}

export function useCategories() {
  return useQuery({
    queryKey: ['categories'],
    queryFn:  categoryService.list,
    staleTime: 1000 * 60 * 5,
  })
}

export function useCreateVocabulary() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateVocabularyRequest) => vocabularyService.create(data),
    onSuccess:  () => qc.invalidateQueries({ queryKey: VOCAB_KEY }),
  })
}

export function useUpdateVocabulary() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateVocabularyRequest }) =>
      vocabularyService.update(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: VOCAB_KEY }),
  })
}

export function useDeleteVocabulary() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => vocabularyService.remove(id),
    onSuccess:  () => qc.invalidateQueries({ queryKey: VOCAB_KEY }),
  })
}
