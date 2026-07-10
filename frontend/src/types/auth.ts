export interface AuthUser {
  userId: number
  name: string
  email: string
}

export interface AuthResponse {
  token: string
  userId: number
  name: string
  email: string
}
