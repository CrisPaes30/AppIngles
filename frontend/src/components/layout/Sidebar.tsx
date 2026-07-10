import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  BookOpen,
  RefreshCw,
  Dumbbell,
  BarChart3,
  FolderOpen,
  Brain,
  X,
  LogOut,
} from 'lucide-react'
import { cn } from '@/utils/cn'
import { Button } from '@/components/ui/Button'
import { useAuth } from '@/contexts/AuthContext'

const NAV_ITEMS = [
  { to: '/dashboard',  icon: LayoutDashboard, label: 'Dashboard'   },
  { to: '/vocabulary', icon: BookOpen,         label: 'Vocabulário' },
  { to: '/review',     icon: RefreshCw,        label: 'Revisão'     },
  { to: '/exercise',   icon: Dumbbell,         label: 'Exercícios'  },
  { to: '/progress',   icon: BarChart3,        label: 'Progresso'   },
  { to: '/categories', icon: FolderOpen,       label: 'Categorias'  },
]

interface Props {
  onClose?: () => void
}

export function Sidebar({ onClose }: Props) {
  const { logout, user } = useAuth()

  return (
    <aside className="flex h-full w-64 flex-col border-r border-border bg-surface">
      {/* Logo */}
      <div className="flex h-16 items-center justify-between border-b border-border px-6">
        <div className="flex items-center gap-3">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand shadow-glow-brand">
            <Brain className="h-5 w-5 text-white" />
          </div>
          <div>
            <p className="text-sm font-bold text-white leading-tight">English Memory</p>
            <p className="text-xs text-slate-500">AI Learning</p>
          </div>
        </div>
        {/* Close button — mobile only */}
        {onClose && (
          <Button variant="ghost" size="sm" onClick={onClose} className="md:hidden h-7 w-7 p-0">
            <X className="h-4 w-4" />
          </Button>
        )}
      </div>

      {/* Nav */}
      <nav className="flex-1 space-y-0.5 overflow-y-auto p-3">
        {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            onClick={onClose}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
                isActive
                  ? 'bg-brand/15 text-brand-light'
                  : 'text-slate-400 hover:bg-surface-hover hover:text-slate-200',
              )
            }
          >
            {({ isActive }) => (
              <>
                <Icon
                  className={cn(
                    'h-4 w-4 flex-shrink-0',
                    isActive ? 'text-brand-light' : 'text-slate-500',
                  )}
                />
                {label}
                {isActive && (
                  <span className="ml-auto h-1.5 w-1.5 rounded-full bg-brand-light" />
                )}
              </>
            )}
          </NavLink>
        ))}
      </nav>

      {/* Footer */}
      <div className="border-t border-border p-4">
        <div className="text-xs text-slate-500 truncate mb-2">{user?.email}</div>
        <button
          onClick={() => void logout()}
          className="flex items-center gap-2 text-sm text-slate-400 hover:text-white transition-colors w-full"
        >
          <LogOut size={16} />
          Sair
        </button>
      </div>
    </aside>
  )
}
