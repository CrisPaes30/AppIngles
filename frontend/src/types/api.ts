export interface ApiResponse<T> {
  success: boolean
  message?: string
  data: T
  timestamp: string
  errors?: string[]
}

export interface PageResponse<T> {
  content: T[]
  currentPage: number
  pageSize: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}
