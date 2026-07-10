import { forwardRef } from 'react'
import type { InputHTMLAttributes } from 'react'
import { cn } from '@/utils/cn'

interface Props extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
}

export const Input = forwardRef<HTMLInputElement, Props>(
  ({ label, error, className, id, ...rest }, ref) => {
    const inputId = id ?? label?.toLowerCase().replace(/\s+/g, '-')
    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label htmlFor={inputId} className="text-sm font-medium text-slate-300">
            {label}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          className={cn(
            'h-10 w-full rounded-lg border border-border bg-surface-elevated px-3 text-sm text-slate-100',
            'placeholder:text-slate-500 focus-ring transition-colors',
            'disabled:opacity-50 disabled:cursor-not-allowed',
            error && 'border-red-500 focus:ring-red-500/30',
            className,
          )}
          {...rest}
        />
        {error && <p className="text-xs text-red-400">{error}</p>}
      </div>
    )
  },
)

Input.displayName = 'Input'
