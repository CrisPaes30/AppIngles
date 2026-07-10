import { useNavigate } from 'react-router-dom'
import {
  BarChart3, TrendingUp, BookOpen, RefreshCw, Flame,
  Plus, Target, AlertTriangle, CheckCircle2, XCircle,
} from 'lucide-react'
import { useProgressDashboard, useWeakWords } from '@/hooks/useProgress'
import { Card }       from '@/components/ui/Card'
import { Badge }      from '@/components/ui/Badge'
import { Button }     from '@/components/ui/Button'
import { Spinner }    from '@/components/ui/Spinner'
import { MasteryBar } from '@/components/ui/MasteryBar'
import { formatMinutes, formatMastery, formatPercent } from '@/utils/format'
import { cn } from '@/utils/cn'
import type { DashboardData, TopMistakeWord } from '@/types/dashboard'
import type { VocabularySummary } from '@/types/vocabulary'

// ─── Distribution bar ─────────────────────────────────────────────────────────

function DistributionBar({ data }: { data: DashboardData }) {
  const total      = data.totalWords || 1
  const dominated  = data.learnedWords
  const learning   = data.learningWords
  const weak       = data.weakWords
  const fresh      = Math.max(total - dominated - learning - weak, 0)

  const segments = [
    { label: 'Dominadas',  count: dominated, color: 'bg-emerald-500', text: 'text-emerald-400' },
    { label: 'Aprendendo', count: learning,  color: 'bg-blue-500',    text: 'text-blue-400'    },
    { label: 'Fracas',     count: weak,      color: 'bg-amber-500',   text: 'text-amber-400'   },
    { label: 'Iniciando',  count: fresh,     color: 'bg-slate-600',   text: 'text-slate-400'   },
  ]

  return (
    <div className="space-y-3">
      <div className="flex h-4 w-full overflow-hidden rounded-full">
        {segments.map(({ label, count, color }) => {
          const pct = (count / total) * 100
          if (pct < 1) return null
          return (
            <div key={label} className={cn('h-full transition-all', color)}
              style={{ width: `${pct}%` }} title={`${label}: ${count}`} />
          )
        })}
      </div>
      <div className="grid grid-cols-2 gap-2 sm:grid-cols-4">
        {segments.map(({ label, count, color, text }) => (
          <div key={label} className="flex items-center gap-2">
            <span className={cn('h-2.5 w-2.5 flex-shrink-0 rounded-full', color)} />
            <div>
              <p className={cn('text-sm font-semibold', text)}>{count}</p>
              <p className="text-[10px] text-slate-600">{label}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

// ─── Weekly mini chart ────────────────────────────────────────────────────────

const DAY_LABELS = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb']

function WeeklyMiniChart({ data }: { data: DashboardData['weeklyChart'] }) {
  const max = Math.max(...data.map((d) => d.wordsReviewed), 1)
  return (
    <div className="space-y-1">
      {data.map((d) => {
        const pct     = Math.round((d.wordsReviewed / max) * 100)
        const date    = new Date(d.date + 'T12:00:00')
        const label   = DAY_LABELS[date.getDay()]
        const isToday = date.toDateString() === new Date().toDateString()
        return (
          <div key={d.date} className="flex items-center gap-3">
            <span className={cn('w-7 text-right text-xs', isToday ? 'text-brand-light' : 'text-slate-600')}>
              {label}
            </span>
            <div className="flex-1 h-2 rounded-full bg-surface-elevated overflow-hidden">
              <div className={cn('h-full rounded-full', isToday ? 'bg-brand' : 'bg-brand/40')}
                style={{ width: `${pct}%` }} />
            </div>
            <span className="w-5 text-xs text-slate-600 text-right">{d.wordsReviewed || ''}</span>
          </div>
        )
      })}
    </div>
  )
}

// ─── Top mistakes table ───────────────────────────────────────────────────────

function TopMistakesSection({ words }: { words: TopMistakeWord[] }) {
  const navigate = useNavigate()
  if (words.length === 0) {
    return (
      <div className="py-6 text-center text-sm text-slate-500">
        Nenhum erro registrado ainda. Continue praticando!
      </div>
    )
  }
  return (
    <div className="space-y-2">
      {words.map((w, i) => (
        <button
          key={w.wordId}
          onClick={() => navigate(`/vocabulary/${w.wordId}`)}
          className="w-full card p-3.5 flex items-center gap-3 hover:border-brand/30 transition-colors text-left"
        >
          <span className="text-xs font-bold text-slate-600 w-4 flex-shrink-0">#{i + 1}</span>
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2">
              <p className="font-mono text-sm font-semibold text-slate-200 truncate">{w.word}</p>
              {w.partOfSpeech && <Badge>{w.partOfSpeech.toLowerCase().replace('_', ' ')}</Badge>}
            </div>
            <p className="text-xs text-slate-500 truncate">{w.translation}</p>
          </div>
          <div className="flex items-center gap-3 flex-shrink-0">
            <div className="flex items-center gap-1 text-emerald-400">
              <CheckCircle2 className="h-3.5 w-3.5" />
              <span className="text-xs">{w.correctCount}</span>
            </div>
            <div className="flex items-center gap-1 text-red-400">
              <XCircle className="h-3.5 w-3.5" />
              <span className="text-xs font-semibold">{w.incorrectCount}</span>
            </div>
            <div className="w-14 text-right">
              <p className="text-xs font-medium text-slate-300">{formatPercent(w.accuracyPct, 0)}</p>
              <p className="text-[10px] text-slate-600">precisão</p>
            </div>
          </div>
        </button>
      ))}
    </div>
  )
}

// ─── Weak words list ──────────────────────────────────────────────────────────

function WeakWordsList({ words }: { words: VocabularySummary[] }) {
  const navigate = useNavigate()
  if (words.length === 0) {
    return (
      <div className="py-6 text-center text-sm text-slate-500">
        Nenhuma palavra fraca. Excelente trabalho!
      </div>
    )
  }
  return (
    <div className="space-y-2">
      {words.map((w) => {
        const { label, color } = formatMastery(w.masteryLevel ?? 0)
        return (
          <div key={w.id} className="card p-3.5 flex items-center gap-3">
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2">
                <p className="font-mono text-sm font-semibold text-slate-200 truncate">{w.word}</p>
                {w.cefrLevel && <Badge>{w.cefrLevel}</Badge>}
              </div>
              <p className="text-xs text-slate-500 truncate">{w.translation}</p>
            </div>
            <div className="text-right flex-shrink-0 min-w-[80px]">
              <p className={cn('text-xs font-medium', color)}>{label}</p>
              <p className="text-[11px] text-slate-600">{formatPercent(w.masteryLevel ?? 0)}</p>
            </div>
            <div className="w-20 flex-shrink-0">
              <MasteryBar level={w.masteryLevel ?? 0} size="sm" />
            </div>
          </div>
        )
      })}
      <div className="pt-2">
        <Button variant="secondary" className="w-full" onClick={() => navigate('/review')}>
          <RefreshCw className="h-4 w-4" /> Revisar palavras fracas
        </Button>
      </div>
    </div>
  )
}

// ─── Stat card mini ───────────────────────────────────────────────────────────

function MiniStat({ icon: Icon, iconColor, label, value, sub }: {
  icon: typeof BookOpen
  iconColor: string
  label: string
  value: string | number
  sub?: string
}) {
  return (
    <Card className="p-4 flex items-center gap-3">
      <div className={cn('flex h-9 w-9 flex-shrink-0 items-center justify-center rounded-lg', iconColor)}>
        <Icon className="h-4 w-4" />
      </div>
      <div className="min-w-0">
        <p className="text-xl font-bold text-slate-100 leading-tight">{value}</p>
        <p className="text-xs text-slate-500 truncate">{label}</p>
        {sub && <p className="text-[10px] text-slate-600 truncate">{sub}</p>}
      </div>
    </Card>
  )
}

// ─── Main ─────────────────────────────────────────────────────────────────────

export function Progress() {
  const { data: dashboard, isLoading: loadingDash } = useProgressDashboard()
  const { data: weakWords, isLoading: loadingWeak } = useWeakWords()

  if (loadingDash) {
    return (
      <div className="flex justify-center py-20">
        <Spinner size="lg" />
      </div>
    )
  }

  if (!dashboard) return null

  const hasData = dashboard.totalWords > 0

  return (
    <div className="space-y-5 animate-fade-in">

      {/* ── Visão geral ──────────────────────────────────────────────────── */}
      <div className="grid gap-3 grid-cols-2 sm:grid-cols-4">
        <MiniStat
          icon={BookOpen} iconColor="bg-brand/20 text-brand-light"
          label="Total de palavras" value={dashboard.totalWords}
        />
        <MiniStat
          icon={Flame} iconColor="bg-orange-500/20 text-orange-400"
          label="Sequência" value={`${dashboard.streakDays}d`}
          sub={dashboard.streakDays > 0 ? 'dias consecutivos' : 'comece hoje!'}
        />
        <MiniStat
          icon={TrendingUp} iconColor="bg-emerald-500/20 text-emerald-400"
          label="Domínio médio" value={formatPercent(dashboard.averageMastery)}
        />
        <MiniStat
          icon={Target} iconColor="bg-blue-500/20 text-blue-400"
          label="Precisão geral" value={formatPercent(dashboard.overallAccuracyPct)}
          sub={hasData ? 'acertos / total' : '—'}
        />
      </div>

      {/* ── Métricas da semana ────────────────────────────────────────────── */}
      <div className="grid gap-3 grid-cols-2 sm:grid-cols-4">
        <MiniStat
          icon={Plus} iconColor="bg-indigo-500/20 text-indigo-400"
          label="Novas esta semana" value={dashboard.newWordsThisWeek}
          sub="palavras adicionadas"
        />
        <MiniStat
          icon={RefreshCw} iconColor="bg-cyan-500/20 text-cyan-400"
          label="Revisadas na semana" value={dashboard.wordsReviewedThisWeek}
          sub="total de revisões"
        />
        <MiniStat
          icon={BarChart3} iconColor="bg-purple-500/20 text-purple-400"
          label="Tempo de estudo" value={formatMinutes(dashboard.totalStudyMinutes)}
          sub="total acumulado"
        />
        {dashboard.weakestPartOfSpeech ? (
          <MiniStat
            icon={AlertTriangle} iconColor="bg-amber-500/20 text-amber-400"
            label="Mais erros em" value={dashboard.weakestPartOfSpeech}
            sub="categoria gramatical"
          />
        ) : (
          <MiniStat
            icon={CheckCircle2} iconColor="bg-emerald-500/20 text-emerald-400"
            label="Categoria fraca" value="—"
            sub="nenhum erro ainda"
          />
        )}
      </div>

      {/* ── Distribuição + Atividade semanal ─────────────────────────────── */}
      <div className="grid gap-5 lg:grid-cols-2">
        <Card className="p-5 space-y-4">
          <div className="flex items-center gap-2">
            <BarChart3 className="h-4 w-4 text-brand-light" />
            <h2 className="text-sm font-semibold text-slate-200">Distribuição de Domínio</h2>
          </div>
          <DistributionBar data={dashboard} />
          <div className="pt-2 border-t border-border space-y-0">
            {[
              { icon: BookOpen,  label: 'Total de palavras',   value: dashboard.totalWords },
              { icon: TrendingUp, label: 'Para revisar hoje',  value: dashboard.wordsToReviewToday,
                sub: dashboard.wordsToReviewToday > 0 ? 'recomendado revisar agora' : 'em dia!' },
              { icon: Flame, label: 'Maior sequência', value: `${dashboard.streakDays} dias` },
            ].map(({ icon: Icon, label, value, sub }) => (
              <div key={label} className="flex items-center justify-between py-2.5 border-b border-border last:border-0">
                <div className="flex items-center gap-3">
                  <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-surface-elevated">
                    <Icon className="h-3.5 w-3.5 text-slate-400" />
                  </div>
                  <div>
                    <p className="text-sm text-slate-300">{label}</p>
                    {sub && <p className="text-xs text-slate-600">{sub}</p>}
                  </div>
                </div>
                <p className="text-sm font-semibold text-slate-100">{value}</p>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-5 space-y-4">
          <div className="flex items-center gap-2">
            <TrendingUp className="h-4 w-4 text-brand-light" />
            <h2 className="text-sm font-semibold text-slate-200">Atividade Semanal</h2>
          </div>
          {dashboard.weeklyChart.length > 0 ? (
            <WeeklyMiniChart data={dashboard.weeklyChart} />
          ) : (
            <p className="text-sm text-slate-600 py-4 text-center">Nenhuma atividade esta semana.</p>
          )}
          <div className="pt-2 border-t border-border space-y-2">
            <p className="text-xs text-slate-500">Domínio geral</p>
            <MasteryBar level={dashboard.averageMastery} showLabel size="md" />
          </div>
        </Card>
      </div>

      {/* ── Principais erros ─────────────────────────────────────────────── */}
      <Card className="p-5 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <XCircle className="h-4 w-4 text-red-400" />
            <h2 className="text-sm font-semibold text-slate-200">Principais Erros</h2>
            <span className="text-xs text-slate-600">palavras com mais erros</span>
          </div>
          {dashboard.topMistakeWords.length > 0 && (
            <Button variant="secondary" size="sm" onClick={() => {}}>
              <RefreshCw className="h-3.5 w-3.5" /> Praticar
            </Button>
          )}
        </div>
        <TopMistakesSection words={dashboard.topMistakeWords} />
      </Card>

      {/* ── Palavras que precisam de atenção ─────────────────────────────── */}
      <Card className="p-5 space-y-4">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <AlertTriangle className="h-4 w-4 text-amber-400" />
            <h2 className="text-sm font-semibold text-slate-200">Palavras que Precisam de Atenção</h2>
            {!loadingWeak && weakWords && weakWords.length > 0 && (
              <Badge variant="warning">{weakWords.length}</Badge>
            )}
          </div>
        </div>
        {loadingWeak ? (
          <div className="flex justify-center py-8">
            <Spinner />
          </div>
        ) : (
          <WeakWordsList words={weakWords ?? []} />
        )}
      </Card>
    </div>
  )
}
