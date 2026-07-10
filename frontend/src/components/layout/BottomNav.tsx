import { NavLink } from 'react-router-dom'
import { LayoutDashboard, BookOpen, RefreshCw, Dumbbell, BarChart3 } from 'lucide-react'
import { cn } from '@/utils/cn'

const NAV_ITEMS = [
  { to: '/dashboard',  icon: LayoutDashboard, label: 'Início'     },
  { to: '/vocabulary', icon: BookOpen,         label: 'Palavras'   },
  { to: '/review',     icon: RefreshCw,        label: 'Revisão'    },
  { to: '/exercise',   icon: Dumbbell,         label: 'Exercícios' },
  { to: '/progress',   icon: BarChart3,        label: 'Progresso'  },
]

export function BottomNav() {
  return (
    <nav className="fixed bottom-0 inset-x-0 z-30 border-t border-border bg-surface md:hidden">
      <div className="flex h-16 items-stretch">
        {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            className={({ isActive }) =>
              cn(
                'flex flex-1 flex-col items-center justify-center gap-0.5 text-[10px] font-medium transition-colors',
                isActive ? 'text-brand-light' : 'text-slate-500',
              )
            }
          >
            {({ isActive }) => (
              <>
                <Icon className={cn('h-5 w-5', isActive ? 'text-brand-light' : 'text-slate-500')} />
                {label}
              </>
            )}
          </NavLink>
        ))}
      </div>
    </nav>
  )
}
