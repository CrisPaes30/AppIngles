import { useQuery } from '@tanstack/react-query'
import { vocabularyService } from '@/services/vocabulary.service'
import { dashboardService } from '@/services/dashboard.service'

export function useWeakWords() {
  return useQuery({
    queryKey: ['vocabulary', 'weak'],
    queryFn:  vocabularyService.listWeak,
  })
}

export function useDueToday() {
  return useQuery({
    queryKey: ['vocabulary', 'due-today'],
    queryFn:  vocabularyService.listDueToday,
  })
}

export function useProgressDashboard() {
  return useQuery({
    queryKey: ['dashboard'],
    queryFn:  dashboardService.get,
    staleTime: 1000 * 60,
  })
}
