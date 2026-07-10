import { forwardRef } from 'react'
import type { SelectHTMLAttributes } from 'react'
import { cn } from '@/utils/cn'

interface Option {
  value: string | number
  label: string
}

interface Props extends SelectHTMLAttributes<HTMLSelectElement> {
  label?: string
  error?: string
  options: Option[]
  placeholder?: string
}

export const Select = forwardRef<HTMLSelectElement, Props>(
  ({ label, error, options, placeholder, className, id, ...rest }, ref) => {
    const selectId = id ?? label?.toLowerCase().replace(/\s+/g, '-')
    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label htmlFor={selectId} className="text-sm font-medium text-slate-300">
            {label}
          </label>
        )}
        <select
          ref={ref}
          id={selectId}
          className={cn(
            'h-10 w-full rounded-lg border border-border bg-surface-elevated px-3 text-sm text-slate-100',
            'focus-ring transition-colors cursor-pointer',
            'disabled:opacity-50 disabled:cursor-not-allowed',
            error && 'border-red-500',
            className,
          )}
          {...rest}
        >
          {placeholder && (
            <option value="" className="bg-surface-card text-slate-400">
              {placeholder}
            </option>
          )}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value} className="bg-surface-card text-slate-100">
              {opt.label}
            </option>
          ))}
        </select>
        {error && <p className="text-xs text-red-400">{error}</p>}
      </div>
    )
  },
)

Select.displayName = 'Select'
