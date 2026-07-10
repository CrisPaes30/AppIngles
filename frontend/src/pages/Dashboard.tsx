import { useNavigate } from 'react-router-dom'
import {
  BookOpen, RefreshCw, Flame, Trophy, Clock,
  CheckCircle, TrendingUp, ArrowRight, AlertCircle,
  Plus, BarChart3, Sparkles,
} from 'lucide-react'
import { useDashboard } from '@/hooks/useDashboard'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { MasteryBar } from '@/components/ui/MasteryBar'
import { formatMinutes, formatPercent } from '@/utils/format'
import type { DailyProgress, DashboardData } from '@/types/dashboard'
import { cn } from '@/utils/cn'

// ─── Weekly bar chart ────────────────────────────────────────────────────────

const DAY_LABELS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']

function WeeklyChart({ data }: { data: DailyProgress[] }) {
  const max = Math.max(...data.map((d) => d.wordsReviewed), 1)
  return (
    <div className="flex items-end gap-2" style={{ height: 100 }}>
      {data.map((d) => {
        const pct = Math.round((d.wordsReviewed / max) * 100)
        const date = new Date(d.date + 'T12:00:00')
        const label = DAY_LABELS[date.getDay()]
        const isToday =
          new Date(d.date + 'T12:00:00').toDateString() === new Date().toDateString()
        return (
          <div key={d.date} className="group flex flex-1 flex-col items-center gap-1">
            {d.wordsReviewed > 0 && (
              <span className="text-[10px] text-slate-500 opacity-0 group-hover:opacity-100 transition-opacity">
                {d.wordsReviewed}
              </span>
            )}
            <div className="relative w-full flex-1 rounded-t overflow-hidden bg-surface-elevated">
              <div
                className={cn(
                  'absolute bottom-0 w-full rounded-t transition-all duration-700',
                  isToday ? 'bg-brand' : 'bg-brand/40',
                )}
                style={{ height: `${pct}%` }}
              />
            </div>
            <span className={cn('text-[10px]', isToday ? 'text-brand-light' : 'text-slate-600')}>
              {label}
            </span>
          </div>
        )
      })}
    </div>
  )
}

// ─── Stat card ───────────────────────────────────────────────────────────────

interface StatCardProps {
  icon: typeof BookOpen
  label: string
  value: string | number
  sub?: string
  highlight?: boolean
  iconColor?: string
}

function StatCard({ icon: Icon, label, value, sub, highlight, iconColor }: StatCardProps) {
  return (
    <Card className={cn('p-5', highlight && 'border-brand/40 shadow-glow-brand/20')}>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-medium text-slate-500 uppercase tracking-wide">{label}</p>
          <p className="mt-1.5 text-2xl font-bold text-slate-100">{value}</p>
          {sub && <p className="mt-0.5 text-xs text-slate-500">{sub}</p>}
        </div>
        <div
          className={cn(
            'flex h-10 w-10 items-center justify-center rounded-lg bg-surface-elevated',
            iconColor,
          )}
        >
          <Icon className="h-5 w-5" />
        </div>
      </div>
    </Card>
  )
}

// ─── Dashboard skeleton ───────────────────────────────────────────────────────

function DashboardSkeleton() {
  return (
    <div className="space-y-6 animate-pulse">
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="h-28 rounded-xl bg-surface-card" />
        ))}
      </div>
      <div className="grid gap-4 lg:grid-cols-3">
        <div className="h-56 rounded-xl bg-surface-card lg:col-span-2" />
        <div className="h-56 rounded-xl bg-surface-card" />
      </div>
    </div>
  )
}

// ─── Onboarding (empty state) ─────────────────────────────────────────────────

const ONBOARDING_STEPS = [
  {
    icon: Plus,
    color: 'text-indigo-400',
    bg: 'bg-indigo-500/10 border-indigo-500/20',
    step: '1',
    title: 'Adicione suas palavras',
    desc: 'Comece com 5 palavras em inglês que você tem dificuldade. Adicione tradução, exemplos e contexto.',
  },
  {
    icon: RefreshCw,
    color: 'text-emerald-400',
    bg: 'bg-emerald-500/10 border-emerald-500/20',
    step: '2',
    title: 'Revise no ritmo certo',
    desc: 'O algoritmo SM-2 agenda as revisões no momento exato antes de esquecer, fixando o vocabulário.',
  },
  {
    icon: BarChart3,
    color: 'text-amber-400',
    bg: 'bg-amber-500/10 border-amber-500/20',
    step: '3',
    title: 'Acompanhe seu domínio',
    desc: 'Veja sua evolução, palavras dominadas e onde você mais erra. Aprenda com dados.',
  },
]

function OnboardingState() {
  const navigate = useNavigate()
  return (
    <div className="flex flex-col items-center gap-8 py-6 animate-fade-in">
      <div className="text-center max-w-md">
        <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-indigo-600/20 border border-indigo-500/30 mb-4">
          <Sparkles className="h-7 w-7 text-indigo-400" />
        </div>
        <h2 className="text-2xl font-bold text-white mb-2">Bem-vindo ao English Memory AI!</h2>
        <p className="text-slate-400 text-sm leading-relaxed">
          Aprenda inglês com repetição espaçada inteligente. Comece adicionando as primeiras palavras que você quer dominar.
        </p>
      </div>

      <div className="grid gap-4 w-full max-w-2xl sm:grid-cols-3">
        {ONBOARDING_STEPS.map(({ icon: Icon, color, bg, step, title, desc }) => (
          <Card key={step} className={cn('p-5 border', bg)}>
            <div className="flex items-center gap-3 mb-3">
              <div className={cn('flex h-8 w-8 items-center justify-center rounded-lg bg-surface-elevated', color)}>
                <Icon className="h-4 w-4" />
              </div>
              <span className="text-xs font-bold text-slate-500 uppercase tracking-widest">Passo {step}</span>
            </div>
            <h3 className="text-sm font-semibold text-slate-200 mb-1">{title}</h3>
            <p className="text-xs text-slate-500 leading-relaxed">{desc}</p>
          </Card>
        ))}
      </div>

      <div className="flex flex-col items-center gap-3">
        <Button size="lg" onClick={() => navigate('/vocabulary')} className="gap-2 px-8">
          <Plus className="h-5 w-5" />
          Adicionar primeira palavra
        </Button>
        <p className="text-xs text-slate-600">Sugestão: comece com 5 palavras que você usa no dia a dia</p>
      </div>
    </div>
  )
}

// ─── Main ─────────────────────────────────────────────────────────────────────

function DashboardContent({ data }: { data: DashboardData }) {
  const navigate = useNavigate()
  const hasDue = data.wordsToReviewToday > 0

  if (data.totalWords === 0) return <OnboardingState />

  return (
    <div className="space-y-6">
      {/* Alert banner when there are words to review */}
      {hasDue && (
        <div className="flex items-center justify-between rounded-lg border border-brand/30 bg-brand/10 px-4 py-3">
          <div className="flex items-center gap-2 text-sm text-brand-light">
            <AlertCircle className="h-4 w-4 flex-shrink-0" />
            <span>
              Você tem <strong>{data.wordsToReviewToday}</strong>{' '}
              {data.wordsToReviewToday === 1 ? 'palavra' : 'palavras'} para revisar hoje.
            </span>
          </div>
          <Button size="sm" onClick={() => navigate('/review')}>
            Revisar agora <ArrowRight className="h-3.5 w-3.5" />
          </Button>
        </div>
      )}

      {/* Stat grid */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-3">
        <StatCard
          icon={BookOpen}
          label="Total de Palavras"
          value={data.totalWords}
          sub={`${data.learnedWords} aprendidas`}
          iconColor="text-brand-light"
        />
        <StatCard
          icon={RefreshCw}
          label="Revisões Hoje"
          value={data.wordsToReviewToday}
          highlight={hasDue}
          iconColor={hasDue ? 'text-amber-400' : 'text-slate-500'}
        />
        <StatCard
          icon={Flame}
          label="Sequência"
          value={`${data.streakDays}d`}
          sub={data.streakDays > 0 ? 'dias consecutivos' : 'comece hoje!'}
          iconColor={data.streakDays > 0 ? 'text-orange-400' : 'text-slate-500'}
        />
        <StatCard
          icon={Trophy}
          label="Domínio Médio"
          value={formatPercent(data.averageMastery)}
          iconColor="text-emerald-400"
        />
        <StatCard
          icon={Clock}
          label="Tempo Total"
          value={formatMinutes(data.totalStudyMinutes)}
          iconColor="text-blue-400"
        />
        <StatCard
          icon={CheckCircle}
          label="Dominadas"
          value={data.learnedWords}
          sub={`de ${data.totalWords} palavras`}
          iconColor="text-emerald-400"
        />
      </div>

      {/* Chart + Quick Actions */}
      <div className="grid gap-4 lg:grid-cols-3">
        {/* Weekly chart */}
        <Card className="p-5 lg:col-span-2">
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <TrendingUp className="h-4 w-4 text-brand-light" />
              <h2 className="text-sm font-semibold text-slate-200">Atividade Semanal</h2>
            </div>
            <span className="text-xs text-slate-500">palavras revisadas por dia</span>
          </div>
          {data.weeklyChart.length > 0 ? (
            <WeeklyChart data={data.weeklyChart} />
          ) : (
            <div className="flex h-24 items-center justify-center">
              <p className="text-sm text-slate-600">Nenhuma atividade esta semana.</p>
            </div>
          )}
        </Card>

        {/* Quick actions */}
        <Card className="flex flex-col gap-3 p-5">
          <h2 className="text-sm font-semibold text-slate-200">Ações Rápidas</h2>
          <Button
            variant="primary"
            className="w-full justify-between"
            onClick={() => navigate('/review')}
            disabled={!hasDue}
          >
            <span className="flex items-center gap-2">
              <RefreshCw className="h-4 w-4" />
              Revisar palavras
            </span>
            {hasDue && (
              <span className="rounded-full bg-white/20 px-2 py-0.5 text-xs">
                {data.wordsToReviewToday}
              </span>
            )}
          </Button>
          <Button
            variant="secondary"
            className="w-full justify-start gap-2"
            onClick={() => navigate('/exercise')}
          >
            <BookOpen className="h-4 w-4" />
            Fazer exercício
          </Button>
          <Button
            variant="secondary"
            className="w-full justify-start gap-2"
            onClick={() => navigate('/vocabulary')}
          >
            <CheckCircle className="h-4 w-4" />
            Adicionar palavra
          </Button>

          {/* Mastery summary */}
          <div className="mt-auto space-y-2 border-t border-border pt-4">
            <div className="flex items-center justify-between text-xs text-slate-500">
              <span>Domínio geral</span>
              <span>{formatPercent(data.averageMastery)}</span>
            </div>
            <MasteryBar level={data.averageMastery} size="md" />
            <div className="flex justify-between text-[10px] text-slate-600">
              <span>{data.weakWords} fracas</span>
              <span>{data.learningWords} aprendendo</span>
              <span>{data.learnedWords} dominadas</span>
            </div>
          </div>
        </Card>
      </div>
    </div>
  )
}

export function Dashboard() {
  const { data, isLoading, isError, refetch } = useDashboard()

  if (isLoading) return <DashboardSkeleton />

  if (isError)
    return (
      <div className="flex flex-col items-center gap-4 py-20">
        <p className="text-slate-400">Erro ao carregar o dashboard.</p>
        <Button variant="secondary" size="sm" onClick={() => refetch()}>
          Tentar novamente
        </Button>
      </div>
    )

  return <DashboardContent data={data!} />
}
