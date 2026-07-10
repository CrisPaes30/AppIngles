import { useMutation, useQueryClient } from '@tanstack/react-query'
import { categoryService } from '@/services/category.service'
import type { CreateCategoryRequest } from '@/services/category.service'

export { useCategories } from './useVocabulary'

export const CATEGORY_KEY = ['categories'] as const

export function useCreateCategory() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (data: CreateCategoryRequest) => categoryService.create(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CATEGORY_KEY })
      qc.invalidateQueries({ queryKey: ['vocabulary'] })
    },
  })
}

export function useUpdateCategory() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<CreateCategoryRequest> }) =>
      categoryService.update(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CATEGORY_KEY })
      qc.invalidateQueries({ queryKey: ['vocabulary'] })
    },
  })
}

export function useDeleteCategory() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => categoryService.remove(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: CATEGORY_KEY })
      qc.invalidateQueries({ queryKey: ['vocabulary'] })
    },
  })
}
