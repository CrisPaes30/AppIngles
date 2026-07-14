import { useState, useEffect, useRef } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import {
  CheckCircle, XCircle, RotateCcw, Shuffle, ArrowRight,
  Send, BookOpen, Search, AlertTriangle, Zap, X, Clock, StopCircle,
} from 'lucide-react'
import { useGenerateExercise, useAnswerExercise, useAnalyzeSentence } from '@/hooks/useExercise'
import { useVocabularyList } from '@/hooks/useVocabulary'
import { useWeakWords } from '@/hooks/useProgress'
import { useDebounce } from '@/hooks/useDebounce'
import { useToast } from '@/contexts/ToastContext'
import { Button }   from '@/components/ui/Button'
import { Select }   from '@/components/ui/Select'
import { Textarea } from '@/components/ui/Textarea'
import { Spinner }  from '@/components/ui/Spinner'
import { Badge }    from '@/components/ui/Badge'
import { cn } from '@/utils/cn'
import { EXERCISE_TYPE_LABELS } from '@/types/exercise'
import type { Exercise, ExerciseAnswer, ExerciseType, SentencePractice } from '@/types/exercise'
import type { VocabularySummary } from '@/types/vocabulary'

// ─── Mode definitions ─────────────────────────────────────────────────────────

type Mode = 'random' | 'word' | 'sentence'

const MODES: { id: Mode; label: string; icon: typeof Shuffle }[] = [
  { id: 'random',   label: 'Aleatório',        icon: Shuffle   },
  { id: 'word',     label: 'Palavra Específica', icon: BookOpen  },
  { id: 'sentence', label: 'Corrigir Frase',    icon: Send      },
]

// ─── Type selector options ────────────────────────────────────────────────────

const TYPE_OPTIONS = [
  { value: '', label: 'Tipo aleatório' },
  ...Object.entries(EXERCISE_TYPE_LABELS).map(([value, label]) => ({ value, label })),
]

// ─── Session duration options ─────────────────────────────────────────────────

const DURATION_OPTIONS = [
  { value: '5',  label: '5 minutos' },
  { value: '10', label: '10 minutos' },
  { value: '15', label: '15 minutos' },
]

function formatRemaining(ms: number): string {
  const totalSec = Math.max(0, Math.ceil(ms / 1000))
  const m = Math.floor(totalSec / 60)
  const s = totalSec % 60
  return `${m}:${s.toString().padStart(2, '0')}`
}

function shuffleTypes(): ExerciseType[] {
  const all = Object.keys(EXERCISE_TYPE_LABELS) as ExerciseType[]
  return [...all].sort(() => Math.random() - 0.5)
}

// ─── Multiple choice ──────────────────────────────────────────────────────────

function MultipleChoice({ options, selected, answered, correct, onSelect }: {
  options: string[]; selected: string | null; answered: boolean
  correct: string | null; onSelect: (o: string) => void
}) {
  return (
    <div className="grid gap-2 sm:grid-cols-2">
      {options.map((opt) => {
        const isSelected = selected === opt
        const isCorrect  = answered && opt === correct
        const isWrong    = answered && isSelected && opt !== correct
        return (
          <button key={opt} onClick={() => !answered && onSelect(opt)} disabled={answered}
            className={cn(
              'rounded-lg border p-4 text-left text-sm transition-all disabled:cursor-default',
              !answered && 'hover:bg-surface-hover hover:border-brand/40 border-border',
              isSelected && !answered && 'border-brand bg-brand/10 text-brand-light',
              isCorrect  && 'border-emerald-500 bg-emerald-500/15 text-emerald-300',
              isWrong    && 'border-red-500 bg-red-500/15 text-red-300',
              !isSelected && !isCorrect && answered && 'border-border opacity-50',
            )}
          >{opt}</button>
        )
      })}
    </div>
  )
}

// ─── Word order ───────────────────────────────────────────────────────────────

function WordOrder({ words, selected, onAdd, onRemove, answered }: {
  words: string[]; selected: string[]; answered: boolean
  onAdd: (w: string) => void; onRemove: (i: number) => void
}) {
  const available = words.filter((w) => !selected.includes(w))
  return (
    <div className="space-y-3">
      <div className="min-h-12 rounded-lg border border-dashed border-border bg-surface-elevated p-3 flex flex-wrap gap-2">
        {selected.length === 0 && <span className="text-sm text-slate-600 self-center">Clique nas palavras para ordenar...</span>}
        {selected.map((w, i) => (
          <button key={`${w}-${i}`} onClick={() => !answered && onRemove(i)} disabled={answered}
            className="rounded-md bg-brand/20 border border-brand/40 px-2.5 py-1 text-sm text-brand-light hover:bg-brand/30 transition-colors disabled:cursor-default">
            {w}
          </button>
        ))}
      </div>
      <div className="flex flex-wrap gap-2">
        {available.map((w, i) => (
          <button key={`${w}-${i}`} onClick={() => !answered && onAdd(w)} disabled={answered}
            className="rounded-md border border-border bg-surface-elevated px-2.5 py-1 text-sm text-slate-300 hover:bg-surface-hover hover:border-slate-500 transition-colors disabled:opacity-40 disabled:cursor-default">
            {w}
          </button>
        ))}
      </div>
    </div>
  )
}

// ─── True / False ─────────────────────────────────────────────────────────────

function TrueFalse({ selected, answered, correct, onSelect }: {
  selected: string | null; answered: boolean
  correct: string | null; onSelect: (v: 'True' | 'False') => void
}) {
  const btn = (value: 'True' | 'False', label: string, color: string) => {
    const isSelected = selected === value
    const isCorrect  = answered && value === correct
    const isWrong    = answered && isSelected && value !== correct
    return (
      <button onClick={() => !answered && onSelect(value)} disabled={answered}
        className={cn(
          'flex-1 rounded-xl border p-6 text-center text-lg font-bold transition-all disabled:cursor-default',
          !answered && `border-border hover:border-${color}-500/50 hover:bg-${color}-500/10`,
          isSelected && !answered && `border-${color}-500 bg-${color}-500/15 text-${color}-300`,
          isCorrect  && 'border-emerald-500 bg-emerald-500/15 text-emerald-300',
          isWrong    && 'border-red-500 bg-red-500/15 text-red-300',
          !isSelected && !isCorrect && answered && 'opacity-40',
        )}>{label}</button>
    )
  }
  return (
    <div className="flex gap-4">
      {btn('True',  'Verdadeiro', 'emerald')}
      {btn('False', 'Falso',      'red')}
    </div>
  )
}

// ─── Result banner ────────────────────────────────────────────────────────────

function ResultBanner({ answer }: { answer: ExerciseAnswer }) {
  return (
    <div className={cn(
      'rounded-lg border p-4 space-y-2 animate-fade-in',
      answer.isCorrect ? 'border-emerald-500/40 bg-emerald-500/10' : 'border-red-500/40 bg-red-500/10',
    )}>
      <div className="flex items-center gap-2">
        {answer.isCorrect
          ? <CheckCircle className="h-5 w-5 text-emerald-400" />
          : <XCircle className="h-5 w-5 text-red-400" />}
        <p className={cn('font-semibold', answer.isCorrect ? 'text-emerald-300' : 'text-red-300')}>
          {answer.isCorrect ? 'Correto!' : 'Incorreto'}
        </p>
      </div>
      {!answer.isCorrect && (
        <p className="text-sm text-slate-300">
          Resposta correta: <span className="font-mono font-semibold text-slate-100">{answer.correctAnswer}</span>
        </p>
      )}
      <p className="text-sm text-slate-400">{answer.explanation}</p>
    </div>
  )
}

// ─── Sentence result ──────────────────────────────────────────────────────────

function SentenceResult({ result }: { result: SentencePractice }) {
  return (
    <div className="space-y-4 animate-fade-in">
      <div className={cn(
        'rounded-lg border p-4',
        result.score >= 70 ? 'border-emerald-500/40 bg-emerald-500/10' : 'border-amber-500/40 bg-amber-500/10',
      )}>
        <div className="flex items-center justify-between mb-3">
          <p className="font-semibold text-slate-200">Análise da frase</p>
          <Badge variant={result.score >= 70 ? 'success' : 'warning'}>{result.score}/100</Badge>
        </div>
        {result.correctedSentence !== result.originalSentence && (
          <div className="space-y-1">
            <p className="text-xs text-slate-500 uppercase tracking-wide">Versão corrigida</p>
            <p className="font-mono text-sm text-emerald-300">{result.correctedSentence}</p>
          </div>
        )}
      </div>
      <div className="card p-4 space-y-3">
        <div>
          <p className="text-xs font-medium uppercase tracking-wide text-slate-500 mb-1">Feedback</p>
          <p className="text-sm text-slate-300">{result.aiFeedback}</p>
        </div>
        {result.grammarExplanation && (
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-slate-500 mb-1">Gramática</p>
            <p className="text-sm text-slate-400">{result.grammarExplanation}</p>
          </div>
        )}
        {result.suggestedSentences.length > 0 && (
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-slate-500 mb-1">Alternativas</p>
            <ul className="space-y-1">
              {result.suggestedSentences.map((s, i) => (
                <li key={i} className="text-sm font-mono text-slate-400">• {s}</li>
              ))}
            </ul>
          </div>
        )}
        {result.newVocabularyFound.length > 0 && (
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-slate-500 mb-1.5">Vocabulário novo detectado</p>
            <div className="flex flex-wrap gap-1.5">
              {result.newVocabularyFound.map((w) => <Badge key={w} variant="brand">{w}</Badge>)}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

// ─── Word picker ──────────────────────────────────────────────────────────────

function WordPicker({ selected, onSelect, onClear }: {
  selected: VocabularySummary | null
  onSelect: (w: VocabularySummary) => void
  onClear: () => void
}) {
  const [search, setSearch] = useState('')
  const debounced = useDebounce(search, 300)
  const { data, isLoading } = useVocabularyList(0, debounced)

  if (selected) {
    return (
      <div className="flex items-center justify-between rounded-lg border border-brand/40 bg-brand/10 px-4 py-3">
        <div>
          <p className="font-mono text-sm font-semibold text-brand-light">{selected.word}</p>
          <p className="text-xs text-slate-400">{selected.translation}</p>
        </div>
        <button onClick={onClear} className="text-slate-500 hover:text-slate-300 transition-colors">
          <X className="h-4 w-4" />
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-2">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-500 pointer-events-none" />
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Buscar palavra..."
          className="h-10 w-full rounded-lg border border-border bg-surface-elevated pl-9 pr-3 text-sm text-slate-100 placeholder:text-slate-500 focus-ring"
        />
      </div>
      <div className="max-h-52 overflow-y-auto rounded-lg border border-border divide-y divide-border">
        {isLoading && (
          <div className="flex justify-center py-4"><Spinner /></div>
        )}
        {!isLoading && data?.content.length === 0 && (
          <p className="py-4 text-center text-sm text-slate-500">Nenhuma palavra encontrada.</p>
        )}
        {data?.content.map((w) => (
          <button key={w.id} onClick={() => onSelect(w)}
            className="w-full flex items-center justify-between px-4 py-2.5 hover:bg-surface-hover transition-colors text-left">
            <div>
              <p className="font-mono text-sm font-semibold text-slate-200">{w.word}</p>
              <p className="text-xs text-slate-500">{w.translation}</p>
            </div>
            {w.cefrLevel && <Badge>{w.cefrLevel}</Badge>}
          </button>
        ))}
      </div>
    </div>
  )
}

// ─── Exercise engine (shared between random + word modes) ─────────────────────

function ExerciseEngine({ mode, wordId, wordName }: { mode: 'random' | 'word'; wordId?: number; wordName?: string }) {
  const toast = useToast()
  const navigate = useNavigate()
  const [typeFilter,  setTypeFilter]  = useState<string>('')
  const [exercise,    setExercise]    = useState<Exercise | null>(null)
  const [answer,      setAnswer]      = useState<ExerciseAnswer | null>(null)
  const [textInput,   setTextInput]   = useState('')
  const [mcSelected,  setMcSelected]  = useState<string | null>(null)
  const [tfSelected,  setTfSelected]  = useState<string | null>(null)
  const [woSelected,  setWoSelected]  = useState<string[]>([])
  const [woWords,     setWoWords]     = useState<string[]>([])
  const startTimeRef = useRef<number>(Date.now())

  // Rotation batch (random mode only): keep the same word for 4 exercises in a row
  const [batchWordId, setBatchWordId] = useState<number | null>(null)
  const [batchCount,  setBatchCount]  = useState(0)
  // Sequência de tipos distintos sorteada pro lote atual (só quando "Tipo aleatório"
  // está selecionado) — evita repetir o mesmo tipo várias vezes seguidas pra mesma palavra.
  const [batchTypeQueue, setBatchTypeQueue] = useState<ExerciseType[]>([])

  // Session (random + word modes): fixed duration, live stats, summary screen
  const [durationChoice,  setDurationChoice]  = useState(10)
  const [sessionStartedAt, setSessionStartedAt] = useState<number | null>(null)
  const [sessionDurationMs, setSessionDurationMs] = useState(10 * 60 * 1000)
  const [stats, setStats] = useState({ total: 0, correct: 0 })
  const [manuallyEnded, setManuallyEnded] = useState(false)
  const [now, setNow] = useState(() => Date.now())

  const generateMut = useGenerateExercise()
  const answerMut   = useAnswerExercise()

  const remainingMs   = sessionStartedAt !== null ? Math.max(0, sessionDurationMs - (now - sessionStartedAt)) : sessionDurationMs
  const timeUp        = sessionStartedAt !== null && remainingMs <= 0
  const sessionEnded  = manuallyEnded || timeUp
  const sessionActive = sessionStartedAt !== null && !sessionEnded
  const phase: 'setup' | 'active' | 'summary' = sessionStartedAt === null ? 'setup' : sessionEnded ? 'summary' : 'active'

  useEffect(() => {
    if (!sessionActive) return
    const id = setInterval(() => setNow(Date.now()), 1000)
    return () => clearInterval(id)
  }, [sessionActive])

  function resetState() {
    setAnswer(null); setTextInput(''); setMcSelected(null)
    setTfSelected(null); setWoSelected([]); startTimeRef.current = Date.now()
  }

  function startSession() {
    setSessionDurationMs(durationChoice * 60 * 1000)
    setSessionStartedAt(Date.now())
    setNow(Date.now())
    setStats({ total: 0, correct: 0 })
    setManuallyEnded(false)
    setBatchWordId(null)
    setBatchCount(0)
    setBatchTypeQueue([])
    setExercise(null)
    setAnswer(null)
  }

  function endSession() {
    setManuallyEnded(true)
  }

  function newSession() {
    setSessionStartedAt(null)
    setStats({ total: 0, correct: 0 })
    setManuallyEnded(false)
    setBatchWordId(null)
    setBatchCount(0)
    setBatchTypeQueue([])
    setExercise(null)
    setAnswer(null)
  }

  async function generate() {
    resetState()
    try {
      const fixedType = (typeFilter as ExerciseType) || undefined
      let ex: Exercise
      if (mode === 'random') {
        if (batchWordId == null || batchCount >= 4) {
          // Novo lote de palavra: se o usuário não fixou um tipo específico,
          // sorteia uma sequência de tipos distintos pra esses 4 exercícios.
          const queue = fixedType ? [] : shuffleTypes()
          const type = fixedType ?? queue[0]
          ex = await generateMut.mutateAsync({ type })
          setBatchWordId(ex.vocabularyWordId)
          setBatchCount(1)
          setBatchTypeQueue(queue)
        } else {
          const type = fixedType ?? batchTypeQueue[batchCount]
          ex = await generateMut.mutateAsync({ type, vocabularyWordId: batchWordId })
          setBatchCount((c) => c + 1)
        }
      } else {
        ex = await generateMut.mutateAsync({ type: fixedType, vocabularyWordId: wordId })
      }
      setExercise(ex)
      if (ex.type === 'WORD_ORDER' && ex.options) {
        setWoWords([...ex.options].sort(() => Math.random() - 0.5))
      }
    } catch (e: any) {
      toast.error(e.message ?? 'Erro ao gerar exercício.')
    }
  }

  async function submit(userAnswer: string) {
    if (!exercise || !userAnswer.trim()) return
    const timeSpent = Math.round((Date.now() - startTimeRef.current) / 1000)
    try {
      const result = await answerMut.mutateAsync({
        id: exercise.id,
        data: { answer: userAnswer.trim(), timeSpentSeconds: timeSpent },
      })
      setAnswer(result)
      setStats((s) => ({ total: s.total + 1, correct: s.correct + (result.isCorrect ? 1 : 0) }))
    } catch (e: any) {
      toast.error(e.message ?? 'Erro ao registrar resposta.')
    }
  }

  useEffect(() => {
    if (sessionActive && !exercise && !generateMut.isPending) generate()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [sessionActive])

  const isAnswered    = answer !== null
  const correctAnswer = answer?.correctAnswer ?? null

  function renderInput() {
    if (!exercise) return null
    switch (exercise.type) {
      case 'MULTIPLE_CHOICE':
        return (
          <MultipleChoice options={exercise.options ?? []} selected={mcSelected}
            answered={isAnswered} correct={correctAnswer}
            onSelect={(o) => { setMcSelected(o); if (!isAnswered) submit(o) }} />
        )
      case 'TRUE_FALSE':
        return (
          <TrueFalse selected={tfSelected} answered={isAnswered} correct={correctAnswer}
            onSelect={(v) => { setTfSelected(v); if (!isAnswered) submit(v) }} />
        )
      case 'WORD_ORDER':
        return (
          <div className="space-y-3">
            <WordOrder words={woWords} selected={woSelected} answered={isAnswered}
              onAdd={(w) => setWoSelected((p) => [...p, w])}
              onRemove={(i) => setWoSelected((p) => p.filter((_, idx) => idx !== i))} />
            {!isAnswered && (
              <Button className="w-full" disabled={woSelected.length === 0}
                onClick={() => submit(woSelected.join(' '))}>
                Confirmar ordem
              </Button>
            )}
          </div>
        )
      default:
        return (
          <div className="space-y-2">
            <textarea value={textInput} onChange={(e) => setTextInput(e.target.value)}
              disabled={isAnswered}
              onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey && !isAnswered) { e.preventDefault(); submit(textInput) } }}
              rows={3} placeholder="Digite sua resposta..."
              className="w-full rounded-lg border border-border bg-surface-elevated px-3 py-2 text-sm text-slate-100 placeholder:text-slate-500 focus-ring resize-none disabled:opacity-50"
            />
            {!isAnswered && (
              <Button className="w-full" disabled={!textInput.trim()}
                onClick={() => submit(textInput)} loading={answerMut.isPending}>
                <Send className="h-4 w-4" /> Responder
              </Button>
            )}
          </div>
        )
    }
  }

  const wordLabel = wordName && (
    <div className="flex items-center gap-2 text-xs text-slate-500">
      <BookOpen className="h-3.5 w-3.5" />
      Praticando: <span className="font-mono text-slate-300">{wordName}</span>
    </div>
  )

  if (phase === 'setup') {
    return (
      <div className="space-y-4">
        {wordLabel}
        <div className="card p-5 space-y-4">
          <div>
            <p className="text-sm font-medium text-slate-200">Duração da sessão</p>
            <p className="text-xs text-slate-500">Pratique por um tempo definido e veja seu resultado ao final.</p>
          </div>
          <Select options={DURATION_OPTIONS} value={String(durationChoice)}
            onChange={(e) => setDurationChoice(Number(e.target.value))} />
          <Button className="w-full" onClick={startSession}>
            Começar sessão <ArrowRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    )
  }

  if (phase === 'summary') {
    const pct = stats.total > 0 ? Math.round((stats.correct / stats.total) * 100) : 0
    const minutes = Math.round(sessionDurationMs / 60000)
    return (
      <div className="space-y-4">
        {wordLabel}
        <div className="card p-6 space-y-4 text-center animate-fade-in">
          <p className="text-lg font-semibold text-slate-100">Sessão concluída!</p>
          <p className="text-sm text-slate-400">
            {stats.total} exercício{stats.total !== 1 ? 's' : ''}, {stats.correct} correto{stats.correct !== 1 ? 's' : ''} ({pct}%), {minutes}min
          </p>
          <div className="flex gap-2">
            <Button className="flex-1" onClick={newSession}>
              <RotateCcw className="h-4 w-4" /> Nova sessão
            </Button>
            <Button variant="secondary" className="flex-1" onClick={() => navigate('/')}>
              Voltar
            </Button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {/* Session timer */}
      <div className="flex items-center justify-between text-xs text-slate-400">
        <span className="flex items-center gap-1.5">
          <Clock className="h-3.5 w-3.5" />
          {formatRemaining(remainingMs)} restantes
        </span>
        <button onClick={endSession}
          className="flex items-center gap-1 text-slate-500 hover:text-red-400 transition-colors">
          <StopCircle className="h-3.5 w-3.5" /> Encerrar sessão
        </button>
      </div>

      {/* Controls */}
      <div className="flex gap-2">
        <div className="flex-1">
          <Select options={TYPE_OPTIONS} value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)} placeholder="" />
        </div>
        <Button onClick={generate} loading={generateMut.isPending} className="flex-shrink-0">
          <Shuffle className="h-4 w-4" />
          {exercise ? 'Novo' : 'Gerar'}
        </Button>
      </div>

      {wordLabel}

      {generateMut.isPending && (
        <div className="flex justify-center py-12"><Spinner size="lg" /></div>
      )}

      {exercise && !generateMut.isPending && (
        <div className="space-y-4">
          <div className="card p-5 space-y-4">
            <div className="flex items-center gap-2">
              <Badge variant="brand">{EXERCISE_TYPE_LABELS[exercise.type]}</Badge>
            </div>
            <p className="text-lg font-semibold text-slate-100">{exercise.question}</p>
            {renderInput()}
          </div>
          {answer && <ResultBanner answer={answer} />}
          {isAnswered && (
            <Button className="w-full animate-fade-in" onClick={generate}>
              Próximo exercício <ArrowRight className="h-4 w-4" />
            </Button>
          )}
        </div>
      )}
    </div>
  )
}

// ─── Weak words banner ────────────────────────────────────────────────────────

function WeakWordsBanner({ onPractice }: { onPractice: (w: VocabularySummary) => void }) {
  const { data: weakWords, isLoading } = useWeakWords()

  if (isLoading || !weakWords || weakWords.length === 0) return null

  function pickRandom() {
    const list = weakWords!
    const picked = list[Math.floor(Math.random() * list.length)]
    onPractice(picked)
  }

  return (
    <div className="flex items-center justify-between rounded-lg border border-amber-500/30 bg-amber-500/10 px-4 py-3">
      <div className="flex items-center gap-2 text-sm text-amber-300">
        <AlertTriangle className="h-4 w-4 flex-shrink-0" />
        <span>
          Você tem <strong>{weakWords.length}</strong> palavra{weakWords.length !== 1 ? 's' : ''} fraca{weakWords.length !== 1 ? 's' : ''}.
        </span>
      </div>
      <Button size="sm" onClick={pickRandom} className="flex-shrink-0 gap-1.5 bg-amber-600 hover:bg-amber-500">
        <Zap className="h-3.5 w-3.5" />
        Exercitar
      </Button>
    </div>
  )
}

// ─── Main ─────────────────────────────────────────────────────────────────────

export function Exercise() {
  const [searchParams] = useSearchParams()
  const initialWordId = searchParams.get('wordId') ? Number(searchParams.get('wordId')) : null

  const [mode, setMode] = useState<Mode>(initialWordId ? 'word' : 'random')

  // Specific word mode state
  const [selectedWord, setSelectedWord] = useState<VocabularySummary | null>(null)
  const [practiceWordId, setPracticeWordId] = useState<number | undefined>(initialWordId ?? undefined)
  const [practiceWordName, setPracticeWordName] = useState<string | undefined>()

  // Sentence mode state
  const [sentence,       setSentence]       = useState('')
  const [sentenceResult, setSentenceResult] = useState<SentencePractice | null>(null)
  const analyzeMut = useAnalyzeSentence()
  const toast = useToast()

  function handleWordSelect(w: VocabularySummary) {
    setSelectedWord(w)
    setPracticeWordId(w.id)
    setPracticeWordName(w.word)
  }

  function handleWeakWordPractice(w: VocabularySummary) {
    setMode('word')
    setSelectedWord(w)
    setPracticeWordId(w.id)
    setPracticeWordName(w.word)
  }

  function handleWordClear() {
    setSelectedWord(null)
    setPracticeWordId(undefined)
    setPracticeWordName(undefined)
  }

  async function analyzeSentence() {
    if (!sentence.trim()) return
    setSentenceResult(null)
    try {
      const result = await analyzeMut.mutateAsync({ sentence })
      setSentenceResult(result)
    } catch (e: any) {
      toast.error(e.message ?? 'Erro ao analisar frase.')
    }
  }

  return (
    <div className="mx-auto max-w-2xl space-y-5 animate-fade-in">

      {/* Mode tabs */}
      <div className="flex gap-1 rounded-lg border border-border bg-surface-elevated p-1">
        {MODES.map(({ id, label, icon: Icon }) => (
          <button key={id} onClick={() => setMode(id)}
            className={cn(
              'flex-1 flex items-center justify-center gap-1.5 rounded-md py-2 text-sm font-medium transition-colors',
              mode === id ? 'bg-brand text-white' : 'text-slate-400 hover:text-slate-200',
            )}
          >
            <Icon className="h-3.5 w-3.5" />
            <span className="hidden sm:inline">{label}</span>
            <span className="sm:hidden">{label.split(' ')[0]}</span>
          </button>
        ))}
      </div>

      {/* ── Random mode ── */}
      {mode === 'random' && (
        <div className="space-y-4">
          <WeakWordsBanner onPractice={handleWeakWordPractice} />
          <ExerciseEngine mode="random" />
        </div>
      )}

      {/* ── Specific word mode ── */}
      {mode === 'word' && (
        <div className="space-y-4">
          <div className="card p-4 space-y-3">
            <p className="text-sm font-medium text-slate-300">Selecione uma palavra para praticar:</p>
            <WordPicker
              selected={selectedWord}
              onSelect={handleWordSelect}
              onClear={handleWordClear}
            />
          </div>
          {practiceWordId && (
            <ExerciseEngine key={practiceWordId} mode="word" wordId={practiceWordId} wordName={practiceWordName} />
          )}
          {!practiceWordId && (
            <div className="flex flex-col items-center gap-3 py-10 text-slate-500">
              <BookOpen className="h-10 w-10 opacity-30" />
              <p className="text-sm">Escolha uma palavra acima para começar</p>
            </div>
          )}
        </div>
      )}

      {/* ── Sentence mode ── */}
      {mode === 'sentence' && (
        <div className="space-y-4">
          <div className="card p-5 space-y-4">
            <div className="space-y-1">
              <p className="text-sm font-medium text-slate-200">Corrija minha frase</p>
              <p className="text-xs text-slate-500">Escreva em inglês e a IA vai corrigir e dar feedback detalhado.</p>
            </div>
            <Textarea label="Sua frase" value={sentence}
              onChange={(e) => setSentence(e.target.value)}
              placeholder="e.g. Yesterday I go to the store and buyed some food."
              rows={3} />
            <Button className="w-full" onClick={analyzeSentence}
              loading={analyzeMut.isPending} disabled={!sentence.trim()}>
              <Send className="h-4 w-4" /> Analisar frase
            </Button>
          </div>
          {sentenceResult && (
            <>
              <SentenceResult result={sentenceResult} />
              <Button variant="secondary" className="w-full"
                onClick={() => { setSentenceResult(null); setSentence('') }}>
                <RotateCcw className="h-4 w-4" /> Nova frase
              </Button>
            </>
          )}
        </div>
      )}
    </div>
  )
}
