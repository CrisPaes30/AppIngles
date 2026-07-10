import { cn } from '@/utils/cn'

interface Props {
  level: number
  showLabel?: boolean
  size?: 'sm' | 'md'
  className?: string
}

function masteryColor(level: number): string {
  if (level >= 90) return 'bg-emerald-500'
  if (level >= 70) return 'bg-blue-500'
  if (level >= 40) return 'bg-amber-500'
  return 'bg-red-500'
}

function masteryLabel(level: number): string {
  if (level >= 90) return 'Dominado'
  if (level >= 70) return 'Bom'
  if (level >= 40) return 'Aprendendo'
  return 'Iniciando'
}

export function MasteryBar({ level, showLabel = false, size = 'md', className }: Props) {
  const clamped = Math.max(0, Math.min(100, level))
  const heights = { sm: 'h-1', md: 'h-1.5' }

  return (
    <div className={cn('flex items-center gap-2', className)}>
      <div className={cn('flex-1 rounded-full bg-surface-elevated overflow-hidden', heights[size])}>
        <div
          className={cn('h-full rounded-full transition-all duration-500', masteryColor(clamped))}
          style={{ width: `${clamped}%` }}
        />
      </div>
      {showLabel && (
        <span className="text-xs text-slate-400 w-20 flex-shrink-0">
          {masteryLabel(clamped)} ({clamped.toFixed(2)}%)
        </span>
      )}
    </div>
  )
}
