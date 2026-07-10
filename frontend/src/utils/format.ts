export function formatDate(iso: string): string {
  return new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(iso))
}

export function formatRelativeDate(iso: string): string {
  const date = new Date(iso)
  const now = new Date()
  const diffMs = date.getTime() - now.getTime()
  const diffDays = Math.round(diffMs / 86_400_000)

  if (diffDays === 0) return 'Hoje'
  if (diffDays === 1) return 'Amanhã'
  if (diffDays === -1) return 'Ontem'
  if (diffDays > 1) return `Em ${diffDays} dias`
  return `Há ${Math.abs(diffDays)} dias`
}

export function formatMinutes(minutes: number): string {
  if (minutes < 60) return `${minutes}min`
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return m === 0 ? `${h}h` : `${h}h ${m}min`
}

export function formatPercent(value: number, decimals = 2): string {
  return value.toFixed(decimals) + '%'
}

export function formatMastery(level: number): { label: string; color: string } {
  if (level >= 90) return { label: 'Dominado',   color: 'text-emerald-400' }
  if (level >= 70) return { label: 'Bom',         color: 'text-blue-400'   }
  if (level >= 40) return { label: 'Aprendendo',  color: 'text-amber-400'  }
  return                   { label: 'Iniciando',   color: 'text-red-400'   }
}
