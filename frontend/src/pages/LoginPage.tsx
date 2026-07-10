import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { useToast } from '@/contexts/ToastContext'

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const { success, error } = useToast()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  useEffect(() => {
    if (isAuthenticated) navigate('/dashboard', { replace: true })
  }, [isAuthenticated, navigate])

  useEffect(() => {
    const saved = localStorage.getItem('em_login_error')
    if (saved) {
      setErrorMsg(saved)
      localStorage.removeItem('em_login_error')
    }
  }, [])

  const handleGoogleLogin = async () => {
    setLoading(true)
    setErrorMsg(null)
    localStorage.removeItem('em_login_error')
    try {
      await login()
      success('Login realizado com sucesso!')
      navigate('/dashboard', { replace: true })
    } catch (e: unknown) {
      const code = (e as any)?.code ?? ''
      const msg = e instanceof Error ? e.message : String(e)
      const full = code ? `[${code}] ${msg}` : msg
      localStorage.setItem('em_login_error', full)
      setErrorMsg(full)
      error(full || 'Falha no login')
    } finally {
      setLoading(false)
    }
  }

  const isBusy = loading

  return (
    <div className="min-h-screen bg-gray-950 flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-10">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-indigo-600 mb-4">
            <span className="text-white text-2xl font-bold">E</span>
          </div>
          <h1 className="text-2xl font-bold text-white">English Memory AI</h1>
          <p className="text-gray-400 mt-2 text-sm">Aprenda vocabulário com repetição espaçada e IA</p>
        </div>

        <div className="bg-gray-900 rounded-2xl border border-gray-800 p-8">
          <h2 className="text-lg font-semibold text-white mb-2">Entrar na sua conta</h2>
          <p className="text-gray-400 text-sm mb-6">Use sua conta Google para continuar</p>

          <button
            onClick={handleGoogleLogin}
            disabled={isBusy}
            className="w-full flex items-center justify-center gap-3 bg-white hover:bg-gray-100 disabled:opacity-60 disabled:cursor-not-allowed text-gray-800 font-medium py-3 px-4 rounded-lg transition-colors"
          >
            {isBusy ? (
              <svg className="animate-spin h-5 w-5 text-gray-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
            ) : (
              <svg width="20" height="20" viewBox="0 0 48 48">
                <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
                <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
                <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
                <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.18 1.48-4.97 2.36-8.16 2.36-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
                <path fill="none" d="M0 0h48v48H0z"/>
              </svg>
            )}
            {loading ? 'Entrando...' : 'Entrar com Google'}
          </button>

          <p className="text-center text-xs text-gray-500 mt-6">
            Ao entrar, você concorda com os termos de uso.
          </p>

          {errorMsg != null && (
            <div className="mt-4 rounded-lg bg-red-900/40 border border-red-700 p-3">
              <p className="text-xs text-red-300 font-mono break-all">{errorMsg || '(erro sem mensagem)'}</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
