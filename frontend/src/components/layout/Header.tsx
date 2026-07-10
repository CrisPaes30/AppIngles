import { useLocation } from 'react-router-dom'
import { Bell, Menu } from 'lucide-react'
import { Button } from '@/components/ui/Button'

const TITLES: Record<string, string> = {
  '/dashboard':  'Dashboard',
  '/vocabulary': 'Vocabulário',
  '/review':     'Revisão',
  '/exercise':   'Exercícios',
  '/progress':   'Progresso',
  '/categories': 'Categorias',
}

interface Props {
  onMenuClick: () => void
}

export function Header({ onMenuClick }: Props) {
  const { pathname } = useLocation()
  const title = TITLES[pathname] ?? 'English Memory AI'

  return (
    <header className="flex h-16 items-center justify-between border-b border-border bg-surface px-4 md:px-6">
      <div className="flex items-center gap-3">
        {/* Hamburger — mobile only */}
        <Button
          variant="ghost"
          size="sm"
          onClick={onMenuClick}
          className="md:hidden h-8 w-8 p-0"
          aria-label="Abrir menu"
        >
          <Menu className="h-5 w-5" />
        </Button>
        <h1 className="text-base font-semibold text-slate-100 md:text-lg">{title}</h1>
      </div>

      <div className="flex items-center gap-2">
        <Button variant="ghost" size="sm" className="h-8 w-8 p-0" aria-label="Notificações">
          <Bell className="h-4 w-4" />
        </Button>
        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-brand text-xs font-bold text-white select-none">
          D
        </div>
      </div>
    </header>
  )
}
