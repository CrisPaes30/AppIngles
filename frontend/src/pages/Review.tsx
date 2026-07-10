import { useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { RotateCcw, CheckCircle2, ArrowRight, Trophy } from 'lucide-react'
import { useReviewCards, useSubmitReview } from '@/hooks/useReview'
import { useToast } from '@/contexts/ToastContext'
import { Button }    from '@/components/ui/Button'
import { Badge }     from '@/components/ui/Badge'
import { Spinner }   from '@/components/ui/Spinner'
import { MasteryBar } from '@/components/ui/MasteryBar'
import { EmptyState } from '@/components/ui/EmptyState'
import { formatPercent } from '@/utils/format'
import { cn } from '@/utils/cn'
import type { ReviewCard, ReviewResult, ReviewQuality } from '@/types/review'

// ─── Progress bar ─────────────────────────────────────────────────────────────

function SessionProgress({ current, total }: { current: number; total: number }) {
  const pct = total === 0 ? 0 : Math.round((current / total) * 100)
  return (
    <div className="space-y-1.5">
      <div className="flex justify-between text-xs text-slate-500">
        <span>{current} de {total} palavras</span>
        <span>{pct}%</span>
      </div>
      <div className="h-1.5 w-full rounded-full bg-surface-elevated overflow-hidden">
        <div
          className="h-full rounded-full bg-brand transition-all duration-500"
          style={{ width: `${pct}%` }}
        />
      </div>
    </div>
  )
}

// ─── Flashcard ────────────────────────────────────────────────────────────────

interface FlashCardProps {
  card: ReviewCard
  flipped: boolean
  onFlip: () => void
}

function FlashCard({ card, flipped, onFlip }: FlashCardProps) {
  return (
    <div
      className="relative cursor-pointer select-none"
      style={{ height: 340, perspective: 1000 }}
      onClick={!flipped ? onFlip : undefined}
    >
      <div className={cn('flip-card-inner w-full h-full', flipped && 'flipped')}>

        {/* Front */}
        <div className="flip-card-face flip-card-front">
          <div className="card flex h-full flex-col items-center justify-center gap-4 p-8 text-center">
            <div className="flex gap-2">
              {card.categoryName && <Badge variant="brand">{card.categoryName}</Badge>}
              {card.cefrLevel && <Badge>{card.cefrLevel}</Badge>}
            </div>
            <p className="font-mono text-4xl font-bold text-slate-100">{card.word}</p>
            {card.pronunciation && (
              <p className="text-lg text-slate-400">{card.pronunciation}</p>
            )}
            {card.ipa && (
              <p className="font-mono text-sm text-slate-500">{card.ipa}</p>
            )}
            <div className="mt-4 flex items-center gap-1.5 text-xs text-slate-600">
              <RotateCcw className="h-3 w-3" />
              <span>Clique para revelar</span>
            </div>
          </div>
        </div>

        {/* Back */}
        <div className="flip-card-face flip-card-back">
          <div className="card flex h-full flex-col gap-4 overflow-y-auto p-6">
            <div className="flex items-center justify-between">
              <p className="font-mono text-xl font-semibold text-slate-300">{card.word}</p>
              {card.partOfSpeech && (
                <Badge>{card.partOfSpeech.toLowerCase().replace('_', ' ')}</Badge>
              )}
            </div>
            <p className="text-3xl font-bold text-slate-100">{card.translation}</p>

            {card.examples.length > 0 && (
              <div className="space-y-1.5">
                <p className="text-xs font-medium uppercase tracking-wide text-slate-600">Exemplos</p>
                {card.examples.slice(0, 2).map((ex, i) => (
                  <p key={i} className="text-sm italic text-slate-400">"{ex}"</p>
                ))}
              </div>
            )}

            {card.synonyms.length > 0 && (
              <div className="flex flex-wrap gap-1.5">
                <span className="text-xs text-slate-600 self-center">Sinônimos:</span>
                {card.synonyms.slice(0, 4).map((s) => (
                  <Badge key={s} variant="default">{s}</Badge>
                ))}
              </div>
            )}

            <div className="mt-auto space-y-1">
              <div className="flex justify-between text-xs text-slate-500">
                <span>Domínio atual</span>
                <span>{formatPercent(card.masteryLevel)}</span>
              </div>
              <MasteryBar level={card.masteryLevel} size="sm" />
              <p className="text-[10px] text-slate-600">
                {card.correctCount}✓ · {card.incorrectCount}✗ · {card.accuracyPercentage.toFixed(0)}% precisão
              </p>
            </div>
          </div>
        </div>

      </div>
    </div>
  )
}

// ─── Quality buttons ──────────────────────────────────────────────────────────

interface QualityBtnProps {
  onSelect: (q: ReviewQuality) => void
  loading: boolean
}

const QUALITIES: { q: ReviewQuality; label: string; sub: string; cls: string }[] = [
  { q: 0, label: 'Errei',   sub: 'Não lembrei',     cls: 'border-red-500/40 hover:bg-red-500/15 hover:border-red-500 text-red-400'   },
  { q: 3, label: 'Difícil', sub: 'Com esforço',      cls: 'border-amber-500/40 hover:bg-amber-500/15 hover:border-amber-500 text-amber-400' },
  { q: 4, label: 'Bom',     sub: 'Com hesitação',    cls: 'border-blue-500/40 hover:bg-blue-500/15 hover:border-blue-500 text-blue-400'  },
  { q: 5, label: 'Fácil',   sub: 'Sem hesitação',    cls: 'border-emerald-500/40 hover:bg-emerald-500/15 hover:border-emerald-500 text-emerald-400' },
]

function QualityButtons({ onSelect, loading }: QualityBtnProps) {
  return (
    <div className="animate-fade-in space-y-2">
      <p className="text-center text-xs text-slate-600">Como foi?</p>
      <div className="grid grid-cols-4 gap-2">
        {QUALITIES.map(({ q, label, sub, cls }) => (
          <button
            key={q}
            onClick={() => onSelect(q)}
            disabled={loading}
            className={cn(
              'flex flex-col items-center gap-0.5 rounded-lg border p-3 transition-all',
              'text-sm font-medium disabled:opacity-50',
              cls,
            )}
          >
            <span>{label}</span>
            <span className="text-[10px] font-normal opacity-60">{sub}</span>
          </button>
        ))}
      </div>
    </div>
  )
}

// ─── Session complete ─────────────────────────────────────────────────────────

interface SessionCompleteProps {
  results: ReviewResult[]
  onRestart: () => void
}

function SessionComplete({ results, onRestart }: SessionCompleteProps) {
  const navigate = useNavigate()
  const correct  = results.filter((r) => r.correct).length
  const total    = results.length
  const pct      = total === 0 ? 0 : Math.round((correct / total) * 100)

  return (
    <div className="mx-auto max-w-md space-y-6 py-8 text-center animate-fade-in">
      <div className="flex justify-center">
        <div className="flex h-20 w-20 items-center justify-center rounded-full bg-brand/20">
          <Trophy className="h-10 w-10 text-brand-light" />
        </div>
      </div>
      <div>
        <p className="text-2xl font-bold text-slate-100">Sessão concluída!</p>
        <p className="mt-1 text-slate-400">Você revisou {total} palavras.</p>
      </div>

      <div className="card p-6 text-left space-y-4">
        <div className="grid grid-cols-3 gap-4 text-center">
          <div>
            <p className="text-2xl font-bold text-emerald-400">{correct}</p>
            <p className="text-xs text-slate-500">Corretas</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-red-400">{total - correct}</p>
            <p className="text-xs text-slate-500">Incorretas</p>
          </div>
          <div>
            <p className="text-2xl font-bold text-brand-light">{pct}%</p>
            <p className="text-xs text-slate-500">Precisão</p>
          </div>
        </div>
        <MasteryBar level={pct} showLabel />
      </div>

      <div className="flex gap-3">
        <Button variant="secondary" className="flex-1" onClick={onRestart}>
          <RotateCcw className="h-4 w-4" /> Revisar de novo
        </Button>
        <Button className="flex-1" onClick={() => navigate('/exercise')}>
          Exercícios <ArrowRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  )
}

// ─── Main ─────────────────────────────────────────────────────────────────────

export function Review() {
  const toast = useToast()
  const { data: cards, isLoading, refetch } = useReviewCards()
  const submitMut = useSubmitReview()

  const [index,   setIndex]   = useState(0)
  const [flipped, setFlipped] = useState(false)
  const [results, setResults] = useState<ReviewResult[]>([])
  const [done,    setDone]    = useState(false)

  const handleQuality = useCallback(async (quality: ReviewQuality) => {
    if (!cards) return
    const card = cards[index]
    try {
      const result = await submitMut.mutateAsync({
        wordId: card.vocabularyWordId,
        data: { quality },
      })
      setResults((prev) => [...prev, result])
      setFlipped(false)
      if (index + 1 >= cards.length) {
        setDone(true)
      } else {
        setIndex((i) => i + 1)
      }
    } catch {
      toast.error('Erro ao registrar resposta. Tente novamente.')
    }
  }, [cards, index, submitMut, toast])

  function restart() {
    setIndex(0)
    setFlipped(false)
    setResults([])
    setDone(false)
    refetch()
  }

  if (isLoading) {
    return (
      <div className="flex justify-center py-20">
        <Spinner size="lg" />
      </div>
    )
  }

  if (!cards || cards.length === 0) {
    return (
      <EmptyState
        icon={CheckCircle2}
        title="Nenhuma palavra para revisar hoje"
        description="Você está em dia! Volte amanhã ou adicione novas palavras."
        action={
          <Button variant="secondary" onClick={() => refetch()}>
            Verificar novamente
          </Button>
        }
      />
    )
  }

  if (done) {
    return <SessionComplete results={results} onRestart={restart} />
  }

  const card = cards[index]

  return (
    <div className="mx-auto max-w-xl space-y-5 animate-fade-in">
      <SessionProgress current={index} total={cards.length} />

      <FlashCard card={card} flipped={flipped} onFlip={() => setFlipped(true)} />

      {flipped ? (
        <QualityButtons onSelect={handleQuality} loading={submitMut.isPending} />
      ) : (
        <p className="text-center text-xs text-slate-600">
          Pense na tradução antes de revelar a resposta
        </p>
      )}

      {/* Skip */}
      {!flipped && (
        <div className="flex justify-center">
          <button
            onClick={() => setFlipped(true)}
            className="text-xs text-slate-600 hover:text-slate-400 underline underline-offset-2"
          >
            Revelar agora
          </button>
        </div>
      )}
    </div>
  )
}
