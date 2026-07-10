import { createContext, useContext, useState, useEffect, type ReactNode } from 'react'
import {
  signInWithPopup, signInWithRedirect, getRedirectResult,
  signOut, GoogleAuthProvider,
} from 'firebase/auth'
import { auth, googleProvider } from '@/config/firebase'
import { authService } from '@/services/auth.service'
import { buildErrorDiagnostic, isFirebaseAuthError, logClientError } from '@/services/clientLog.service'
import type { AuthUser } from '@/types/auth'

const TOKEN_KEY = 'em_token'

interface AuthContextValue {
  user: AuthUser | null
  token: string | null
  isAuthenticated: boolean
  redirectLoading: boolean
  login: () => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

function decodeToken(token: string): AuthUser | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return {
      userId: Number(payload.sub),
      email: payload.email ?? '',
      name: payload.name ?? payload.email ?? '',
    }
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [user, setUser] = useState<AuthUser | null>(() => {
    const t = localStorage.getItem(TOKEN_KEY)
    return t ? decodeToken(t) : null
  })
  const [redirectLoading, setRedirectLoading] = useState(true)

  useEffect(() => {
    getRedirectResult(auth)
      .then(async (result) => {
        if (!result) return
        const credential = GoogleAuthProvider.credentialFromResult(result)
        const idToken = credential?.idToken
        if (!idToken) {
          localStorage.setItem('em_login_error', 'redirect: idToken ausente')
          logClientError('login-redirect', 'redirect: idToken ausente')
          return
        }
        const res = await authService.loginWithGoogle(idToken)
        localStorage.setItem(TOKEN_KEY, res.token)
        setToken(res.token)
        setUser({ userId: res.userId, name: res.name, email: res.email })
      })
      .catch((e: unknown) => {
        const diagnostic = buildErrorDiagnostic(e)
        localStorage.setItem('em_login_error', `redirect-err: ${diagnostic}`)
        logClientError(isFirebaseAuthError(e) ? 'login-redirect' : 'login-api-call', `redirect-err: ${diagnostic}`)
      })
      .finally(() => setRedirectLoading(false))
  }, [])

  const login = async () => {
    try {
      const result = await signInWithPopup(auth, googleProvider)
      const credential = GoogleAuthProvider.credentialFromResult(result)
      const idToken = credential?.idToken
      if (!idToken) throw new Error('Não foi possível obter o token do Google')
      const res = await authService.loginWithGoogle(idToken)
      localStorage.setItem(TOKEN_KEY, res.token)
      setToken(res.token)
      setUser({ userId: res.userId, name: res.name, email: res.email })
    } catch (e: unknown) {
      if ((e as any)?.code === 'auth/popup-blocked') {
        await signInWithRedirect(auth, googleProvider)
        return
      }
      throw e
    }
  }

  const logout = async () => {
    await signOut(auth)
    localStorage.removeItem(TOKEN_KEY)
    setToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated: !!token, redirectLoading, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth deve ser usado dentro de AuthProvider')
  return ctx
}
