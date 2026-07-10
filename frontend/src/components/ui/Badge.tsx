import { cn } from '@/utils/cn'
import type { HTMLAttributes } from 'react'

export type BadgeVariant = 'default' | 'brand' | 'success' | 'warning' | 'danger'

const variants: Record<BadgeVariant, string> = {
  default: 'bg-surface-elevated text-slate-300 border border-border',
  brand:   'bg-brand/20 text-brand-light border border-brand/30',
  success: 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30',
  warning: 'bg-amber-500/20 text-amber-400 border border-amber-500/30',
  danger:  'bg-red-500/20 text-red-400 border border-red-500/30',
}

interface Props extends HTMLAttributes<HTMLSpanElement> {
  variant?: BadgeVariant
}

export function Badge({ variant = 'default', className, children, ...rest }: Props) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium',
        variants[variant],
        className,
      )}
      {...rest}
    >
      {children}
    </span>
  )
}
