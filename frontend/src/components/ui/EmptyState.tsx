import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'

interface Props {
  icon: LucideIcon
  title: string
  description?: string
  action?: ReactNode
}

export function EmptyState({ icon: Icon, title, description, action }: Props) {
  return (
    <div className="flex flex-col items-center justify-center gap-4 py-16 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-full bg-surface-elevated">
        <Icon className="h-8 w-8 text-slate-500" />
      </div>
      <div className="space-y-1">
        <p className="font-medium text-slate-300">{title}</p>
        {description && <p className="text-sm text-slate-500">{description}</p>}
      </div>
      {action}
    </div>
  )
}
