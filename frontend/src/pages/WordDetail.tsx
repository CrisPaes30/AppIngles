import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  ArrowLeft, Pencil, BookOpen, Volume2, CalendarClock,
  CheckCircle2, XCircle, BarChart3, Tag, Layers, Dumbbell,
  RefreshCw, Brain, AlertTriangle, Lightbulb, Link2, Save, X,
} from 'lucide-react'
import { useVocabularyDetail, useUpdateVocabulary } from '@/hooks/useVocabulary'
import { useToast } from '@/contexts/ToastContext'
import { Card }      from '@/components/ui/Card'
import { Button }    from '@/components/ui/Button'
import { Badge }     from '@/components/ui/Badge'
import { MasteryBar } from '@/components/ui/MasteryBar'
import { Spinner }   from '@/components/ui/Spinner'
import { Textarea }  from '@/components/ui/Textarea'
import { formatPercent, formatRelativeDate, formatMastery } from '@/utils/format'
import { cn } from '@/utils/cn'

const CEFR_BADGE: Record<string, string> = {
  A1: 'text-emerald-300 bg-emerald-500/15 border-emerald-500/30',
  A2: 'text-emerald-400 bg-emerald-500/20 border-emerald-500/40',
  B1: 'text-blue-300   bg-blue-500/15    border-blue-500/30',
  B2: 'text-blue-400   bg-blue-500/20    border-blue-500/40',
  C1: 'text-purple-300 bg-purple-500/15  border-purple-500/30',
  C2: 'text-purple-400 bg-purple-500/20  border-purple-500/40',
}

const POS_LABEL: Record<string, string> = {
  NOUN: 'Substantivo', VERB: 'Verbo', ADJECTIVE: 'Adjetivo',
  ADVERB: 'Advérbio', PREPOSITION: 'Preposição', CONJUNCTION: 'Conjunção',
  INTERJECTION: 'Interjeição', PHRASAL_VERB: 'Phrasal Verb',
  EXPRESSION: 'Expressão', OTHER: 'Outro',
}

const DIFFICULTY_STARS = (n: number) => '★'.repeat(n) + '☆'.repeat(5 - n)

// ─── Inline editable text area ────────────────────────────────────────────────

interface EditableFieldProps {
  label: string
  value: string
  onSave: (v: string) => Promise<void>
  placeholder?: string
  rows?: number
}

function EditableField({ label, value, onSave, placeholder, rows = 3 }: EditableFieldProps) {
  const [editing, setEditing] = useState(false)
  const [draft, setDraft]     = useState(value)
  const [saving, setSaving]   = useState(false)

  async function handleSave() {
    setSaving(true)
    try {
      await onSave(draft)
      setEditing(false)
    } catch {
      // error toast already shown by patch()
    } finally {
      setSaving(false)
    }
  }

  if (editing) {
    return (
      <div className="space-y-2">
        <Textarea
          label={label}
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          rows={rows}
          placeholder={placeholder}
          autoFocus
        />
        <div className="flex gap-2 justify-end">
          <Button variant="ghost" size="sm" onClick={() => { setDraft(value); setEditing(false) }} disabled={saving}>
            <X className="h-3.5 w-3.5" /> Cancelar
          </Button>
          <Button size="sm" onClick={handleSave} loading={saving}>
            <Save className="h-3.5 w-3.5" /> Salvar
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div
      className="group cursor-pointer rounded-lg border border-transparent px-3 py-2 -mx-3 hover:border-border hover:bg-surface-elevated transition-colors"
      onClick={() => { setDraft(value); setEditing(true) }}
    >
      <p className="text-xs font-medium text-slate-500 uppercase tracking-wide mb-1">{label}</p>
      {value ? (
        <p className="text-sm text-slate-300 leading-relaxed whitespace-pre-wrap">{value}</p>
      ) : (
        <p className="text-sm text-slate-600 italic">{placeholder ?? 'Clique para adicionar...'}</p>
      )}
      <Pencil className="h-3 w-3 text-slate-600 mt-1 opacity-0 group-hover:opacity-100 transition-opacity" />
    </div>
  )
}

// ─── Main page ────────────────────────────────────────────────────────────────

export function WordDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const toast = useToast()
  const wordId = id ? Number(id) : null
  const { data: word, isLoading, isError } = useVocabularyDetail(wordId)
  const updateMut = useUpdateVocabulary()

  if (isLoading) return <div className="flex justify-center py-20"><Spinner size="lg" /></div>
  if (isError || !word) {
    return (
      <div className="flex flex-col items-center gap-4 py-20">
        <p className="text-slate-400">Palavra não encontrada.</p>
        <Button variant="secondary" size="sm" onClick={() => navigate('/vocabulary')}>Voltar ao vocabulário</Button>
      </div>
    )
  }

  const rs = word.reviewSchedule
  const mastery = rs
    ? Math.round((rs.correctCount / Math.max(rs.correctCount + rs.incorrectCount, 1)) * 100)
    : 0
  const masteryInfo = formatMastery(mastery)
  const totalReviews = rs ? rs.correctCount + rs.incorrectCount : 0

  async function patch(data: Record<string, unknown>) {
    try {
      await updateMut.mutateAsync({ id: word!.id, data: data as any })
      toast.success('Salvo!')
    } catch (e: any) {
      toast.error(e.message ?? 'Erro ao salvar.')
      throw e
    }
  }

  return (
    <div className="max-w-2xl mx-auto space-y-5 animate-fade-in pb-6">
      {/* Back nav */}
      <div className="flex items-center justify-between">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-sm text-slate-400 hover:text-slate-200 transition-colors"
        >
          <ArrowLeft className="h-4 w-4" /> Voltar
        </button>
      </div>

      {/* ── Header ── */}
      <Card className="p-6">
        <div className="flex items-start justify-between gap-4 mb-3">
          <div>
            <h1 className="text-3xl font-bold font-mono text-white">{word.word}</h1>
            <p className="text-lg text-slate-400 mt-1">{word.translation}</p>
            {(word.pronunciation || word.ipa) && (
              <p className="flex items-center gap-2 text-sm text-slate-500 font-mono mt-1">
                <Volume2 className="h-3.5 w-3.5" />
                {word.pronunciation && <span>{word.pronunciation}</span>}
                {word.ipa && <span className="text-slate-600">{word.ipa}</span>}
              </p>
            )}
          </div>
          <div className="flex flex-col items-end gap-2 flex-shrink-0">
            {word.cefrLevel && (
              <span className={cn('inline-flex items-center rounded-full border px-2.5 py-1 text-sm font-bold',
                CEFR_BADGE[word.cefrLevel])}>
                {word.cefrLevel}
              </span>
            )}
            {word.difficulty && (
              <span className="text-amber-400 text-sm tracking-tight">{DIFFICULTY_STARS(word.difficulty)}</span>
            )}
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          {word.partOfSpeech && <Badge>{POS_LABEL[word.partOfSpeech] ?? word.partOfSpeech}</Badge>}
          {word.category && (
            <Badge variant="brand"><Tag className="h-3 w-3 mr-1" />{word.category.name}</Badge>
          )}
        </div>
      </Card>

      {/* ── Significado ── */}
      {word.meaning && (
        <Card className="p-5">
          <div className="flex items-center gap-2 mb-2">
            <BookOpen className="h-4 w-4 text-brand-light" />
            <h2 className="text-sm font-semibold text-slate-200">Significado</h2>
          </div>
          <p className="text-sm text-slate-300 leading-relaxed">{word.meaning}</p>
        </Card>
      )}

      {/* ── Exemplos ── */}
      {word.examples.length > 0 && (
        <Card className="p-5">
          <div className="flex items-center gap-2 mb-3">
            <BookOpen className="h-4 w-4 text-brand-light" />
            <h2 className="text-sm font-semibold text-slate-200">Como usar</h2>
          </div>
          <ul className="space-y-2">
            {word.examples.map((ex, i) => (
              <li key={i} className="flex gap-2 text-sm text-slate-400">
                <span className="text-brand-light/60 flex-shrink-0 mt-0.5">→</span>
                <span className="italic">{ex}</span>
              </li>
            ))}
          </ul>
        </Card>
      )}

      {/* ── Erros comuns ── */}
      {word.commonErrors && word.commonErrors.length > 0 && (
        <Card className="p-5">
          <div className="flex items-center gap-2 mb-3">
            <AlertTriangle className="h-4 w-4 text-red-400" />
            <h2 className="text-sm font-semibold text-slate-200">Erros comuns</h2>
          </div>
          <ul className="space-y-1.5">
            {word.commonErrors.map((err, i) => (
              <li key={i} className="flex gap-2 text-sm text-red-300">
                <span className="flex-shrink-0 mt-0.5">✗</span>
                <span>{err}</span>
              </li>
            ))}
          </ul>
        </Card>
      )}

      {/* ── Dicas de uso ── */}
      {word.usageTips && word.usageTips.length > 0 && (
        <Card className="p-5">
          <div className="flex items-center gap-2 mb-3">
            <Lightbulb className="h-4 w-4 text-amber-400" />
            <h2 className="text-sm font-semibold text-slate-200">Dicas de uso</h2>
          </div>
          <ul className="space-y-1.5">
            {word.usageTips.map((tip, i) => (
              <li key={i} className="flex gap-2 text-sm text-slate-400">
                <span className="text-amber-400/60 flex-shrink-0 mt-0.5">•</span>
                <span>{tip}</span>
              </li>
            ))}
          </ul>
        </Card>
      )}

      {/* ── Collocations / Phrasal verbs ── */}
      {(word.collocations.length > 0 || word.relatedPhrasalVerbs.length > 0) && (
        <Card className="p-5">
          <div className="flex items-center gap-2 mb-3">
            <Link2 className="h-4 w-4 text-brand-light" />
            <h2 className="text-sm font-semibold text-slate-200">Combinações & Phrasal Verbs</h2>
          </div>
          {word.collocations.length > 0 && (
            <div className="mb-3">
              <p className="text-xs text-slate-500 uppercase tracking-wide mb-1.5">Collocations</p>
              <div className="flex flex-wrap gap-1.5">
                {word.collocations.map((c) => (
                  <span key={c} className="rounded-md bg-blue-500/10 border border-blue-500/20 px-2.5 py-0.5 text-xs text-blue-300">{c}</span>
                ))}
              </div>
            </div>
          )}
          {word.relatedPhrasalVerbs.length > 0 && (
            <div>
              <p className="text-xs text-slate-500 uppercase tracking-wide mb-1.5">Phrasal Verbs</p>
              <div className="flex flex-wrap gap-1.5">
                {word.relatedPhrasalVerbs.map((p) => (
                  <span key={p} className="rounded-md bg-purple-500/10 border border-purple-500/20 px-2.5 py-0.5 text-xs text-purple-300">{p}</span>
                ))}
              </div>
            </div>
          )}
        </Card>
      )}

      {/* ── Sinônimos & Antônimos ── */}
      {(word.synonyms.length > 0 || word.antonyms.length > 0) && (
        <Card className="p-5">
          <div className="flex items-center gap-2 mb-3">
            <Layers className="h-4 w-4 text-brand-light" />
            <h2 className="text-sm font-semibold text-slate-200">Sinônimos & Antônimos</h2>
          </div>
          <div className="grid grid-cols-2 gap-4">
            {word.synonyms.length > 0 && (
              <div>
                <div className="flex items-center gap-1.5 text-xs text-emerald-400 mb-2">
                  <CheckCircle2 className="h-3.5 w-3.5" /> Sinônimos
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {word.synonyms.map((s) => (
                    <span key={s} className="rounded-full bg-emerald-500/10 border border-emerald-500/20 px-2.5 py-0.5 text-xs text-emerald-300">{s}</span>
                  ))}
                </div>
              </div>
            )}
            {word.antonyms.length > 0 && (
              <div>
                <div className="flex items-center gap-1.5 text-xs text-red-400 mb-2">
                  <XCircle className="h-3.5 w-3.5" /> Antônimos
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {word.antonyms.map((a) => (
                    <span key={a} className="rounded-full bg-red-500/10 border border-red-500/20 px-2.5 py-0.5 text-xs text-red-300">{a}</span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </Card>
      )}

      {/* ── Minha forma de lembrar (editable) ── */}
      <Card className="p-5">
        <div className="flex items-center gap-2 mb-3">
          <Brain className="h-4 w-4 text-brand-light" />
          <h2 className="text-sm font-semibold text-slate-200">Minha forma de lembrar</h2>
        </div>
        <EditableField
          label=""
          value={word.personalMemory ?? ''}
          onSave={(v) => patch({ personalMemory: v.trim() })}
          placeholder="Escreva uma associação, história ou dica pessoal para memorizar esta palavra..."
          rows={3}
        />
      </Card>

      {/* ── Notas (editable) ── */}
      <Card className="p-5">
        <h2 className="text-sm font-semibold text-slate-200 mb-3">Notas pessoais</h2>
        <EditableField
          label=""
          value={word.notes ?? ''}
          onSave={(v) => patch({ notes: v.trim() })}
          placeholder="Observações, contexto de onde você viu a palavra, etc..."
          rows={3}
        />
      </Card>

      {/* ── Progresso ── */}
      {rs && (
        <Card className="p-5">
          <div className="flex items-center gap-2 mb-4">
            <BarChart3 className="h-4 w-4 text-brand-light" />
            <h2 className="text-sm font-semibold text-slate-200">Progresso de aprendizado</h2>
          </div>
          <div className="grid grid-cols-3 gap-4 mb-4">
            <div className="text-center">
              <p className="text-2xl font-bold text-slate-100">{totalReviews}</p>
              <p className="text-xs text-slate-500 mt-0.5">Revisões</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-emerald-400">{rs.correctCount}</p>
              <p className="text-xs text-slate-500 mt-0.5">Corretas</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-red-400">{rs.incorrectCount}</p>
              <p className="text-xs text-slate-500 mt-0.5">Erradas</p>
            </div>
          </div>
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span className="text-slate-400">Domínio</span>
              <span className={cn('font-medium', masteryInfo.color)}>
                {formatPercent(mastery)} · {masteryInfo.label}
              </span>
            </div>
            <MasteryBar level={mastery} size="md" />
          </div>
          {rs.nextReviewDate && (
            <div className="mt-4 flex items-center gap-2 text-sm text-slate-500">
              <CalendarClock className="h-4 w-4" />
              <span>Próxima revisão: <span className="text-slate-300">{formatRelativeDate(rs.nextReviewDate)}</span></span>
            </div>
          )}
          <div className="mt-2 flex items-center gap-4 text-xs text-slate-600">
            <span>Intervalo atual: {rs.intervalDays}d</span>
            <span>Repetições: {rs.repetitions}</span>
          </div>
        </Card>
      )}

      {/* ── CTA buttons ── */}
      <div className="flex gap-3 pt-2">
        <Button className="flex-1 gap-2" onClick={() => navigate('/review')}>
          <RefreshCw className="h-4 w-4" /> Revisar agora
        </Button>
        <Button variant="secondary" className="flex-1 gap-2" onClick={() => navigate(`/exercise?wordId=${word.id}`)}>
          <Dumbbell className="h-4 w-4" /> Fazer exercício
        </Button>
      </div>
    </div>
  )
}
