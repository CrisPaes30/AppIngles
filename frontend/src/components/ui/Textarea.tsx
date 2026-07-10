import { forwardRef } from 'react'
import type { TextareaHTMLAttributes } from 'react'
import { cn } from '@/utils/cn'

interface Props extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string
  error?: string
}

export const Textarea = forwardRef<HTMLTextAreaElement, Props>(
  ({ label, error, className, id, ...rest }, ref) => {
    const textareaId = id ?? label?.toLowerCase().replace(/\s+/g, '-')
    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label htmlFor={textareaId} className="text-sm font-medium text-slate-300">
            {label}
          </label>
        )}
        <textarea
          ref={ref}
          id={textareaId}
          rows={3}
          className={cn(
            'w-full rounded-lg border border-border bg-surface-elevated px-3 py-2 text-sm text-slate-100',
            'placeholder:text-slate-500 focus-ring transition-colors resize-none',
            'disabled:opacity-50 disabled:cursor-not-allowed',
            error && 'border-red-500',
            className,
          )}
          {...rest}
        />
        {error && <p className="text-xs text-red-400">{error}</p>}
      </div>
    )
  },
)

Textarea.displayName = 'Textarea'
