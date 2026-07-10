import { createContext, useCallback, useContext, useState } from 'react'
import type { ReactNode } from 'react'
import { X, CheckCircle, AlertCircle, Info } from 'lucide-react'
import { cn } from '@/utils/cn'

type ToastType = 'success' | 'error' | 'info'

interface Toast {
  id: string
  type: ToastType
  message: string
}

interface ToastContextValue {
  success: (message: string) => void
  error: (message: string) => void
  info: (message: string) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

const ICONS: Record<ToastType, typeof CheckCircle> = {
  success: CheckCircle,
  error:   AlertCircle,
  info:    Info,
}

const STYLES: Record<ToastType, string> = {
  success: 'border-emerald-500/40 bg-emerald-500/10 text-emerald-300',
  error:   'border-red-500/40 bg-red-500/10 text-red-300',
  info:    'border-brand/40 bg-brand/10 text-brand-light',
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])

  const add = useCallback((type: ToastType, message: string) => {
    const id = Math.random().toString(36).slice(2)
    setToasts((prev) => [...prev, { id, type, message }])
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), 4000)
  }, [])

  const remove = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const ctx: ToastContextValue = {
    success: (m) => add('success', m),
    error:   (m) => add('error', m),
    info:    (m) => add('info', m),
  }

  return (
    <ToastContext.Provider value={ctx}>
      {children}
      <div className="pointer-events-none fixed bottom-20 right-4 z-50 flex flex-col gap-2 md:bottom-4">
        {toasts.map(({ id, type, message }) => {
          const Icon = ICONS[type]
          return (
            <div
              key={id}
              className={cn(
                'pointer-events-auto flex items-start gap-3 rounded-lg border p-3.5 shadow-card',
                'w-80 animate-slide-up text-sm',
                STYLES[type],
              )}
            >
              <Icon className="mt-0.5 h-4 w-4 flex-shrink-0" />
              <p className="flex-1">{message}</p>
              <button onClick={() => remove(id)} className="flex-shrink-0 opacity-60 hover:opacity-100">
                <X className="h-4 w-4" />
              </button>
            </div>
          )
        })}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext)
  if (!ctx) throw new Error('useToast must be used inside ToastProvider')
  return ctx
}
