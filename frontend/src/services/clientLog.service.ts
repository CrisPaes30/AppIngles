import axios from 'axios'

const CLIENT_LOG_URL = `${import.meta.env.VITE_API_URL || '/api'}/client-log`

export function logClientError(context: string, message: string): void {
  axios
    .post(
      CLIENT_LOG_URL,
      {
        context: context.slice(0, 100),
        message: message.slice(0, 2000),
        url: window.location.href.slice(0, 500),
        userAgent: navigator.userAgent.slice(0, 300),
      },
      { headers: { 'ngrok-skip-browser-warning': 'true' } },
    )
    .catch(() => {})
}

export function buildErrorDiagnostic(e: unknown): string {
  const code = (e as { code?: string })?.code ?? ''
  const msg = e instanceof Error ? e.message : String(e)
  const extra = (e as { diagnostic?: string })?.diagnostic
  const base = code ? `[${code}] ${msg}` : msg
  return extra ? `${base} | ${extra}` : base
}

export function isFirebaseAuthError(e: unknown): boolean {
  const code = (e as { code?: string })?.code
  return typeof code === 'string' && code.startsWith('auth/')
}
