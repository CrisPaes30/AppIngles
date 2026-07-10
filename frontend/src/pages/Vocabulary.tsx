import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Plus, Search, Pencil, Trash2, BookOpen,
  ChevronLeft, ChevronRight, X, Sparkles, AlertCircle,
} from 'lucide-react'
import {
  useVocabularyList,
  useVocabularyDetail,
  useCategories,
  useCreateVocabulary,
  useUpdateVocabulary,
  useDeleteVocabulary,
  useEnrichVocabulary,
} from '@/hooks/useVocabulary'
import { useDebounce }    from '@/hooks/useDebounce'
import { useToast }       from '@/contexts/ToastContext'
import { Button }         from '@/components/ui/Button'
import { Input }          from '@/components/ui/Input'
import { Select }         from '@/components/ui/Select'
import { Textarea }       from '@/components/ui/Textarea'
import { Card }           from '@/components/ui/Card'
import { Badge }          from '@/components/ui/Badge'
import { Spinner }        from '@/components/ui/Spinner'
import { Modal }          from '@/components/ui/Modal'
import { ConfirmDialog }  from '@/components/ui/ConfirmDialog'
import { MasteryBar }     from '@/components/ui/MasteryBar'
import { EmptyState }     from '@/components/ui/EmptyState'
import { formatRelativeDate, formatPercent } from '@/utils/format'
import { cn } from '@/utils/cn'
import type {
  VocabularySummary, CefrLevel, PartOfSpeech,
  WordDetails, CreateVocabularyRequest, UpdateVocabularyRequest,
} from '@/types/vocabulary'

// ─── Constants ────────────────────────────────────────────────────────────────

const CEFR_OPTIONS = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2'].map((v) => ({ value: v, label: v }))

const POS_OPTIONS: { value: PartOfSpeech; label: string }[] = [
  { value: 'NOUN',         label: 'Substantivo'  },
  { value: 'VERB',         label: 'Verbo'         },
  { value: 'ADJECTIVE',    label: 'Adjetivo'      },
  { value: 'ADVERB',       label: 'Advérbio'      },
  { value: 'PREPOSITION',  label: 'Preposição'    },
  { value: 'CONJUNCTION',  label: 'Conjunção'     },
  { value: 'INTERJECTION', label: 'Interjeição'   },
  { value: 'PHRASAL_VERB', label: 'Phrasal Verb'  },
  { value: 'EXPRESSION',   label: 'Expressão'     },
  { value: 'OTHER',        label: 'Outro'          },
]

const DIFFICULTY_OPTIONS = [1, 2, 3, 4, 5].map((v) => ({
  value: v,
  label: `${v} — ${'★'.repeat(v)}${'☆'.repeat(5 - v)}`,
}))

const CEFR_BADGE: Record<CefrLevel, string> = {
  A1: 'text-emerald-300 bg-emerald-500/15 border-emerald-500/30',
  A2: 'text-emerald-400 bg-emerald-500/20 border-emerald-500/40',
  B1: 'text-blue-300   bg-blue-500/15    border-blue-500/30',
  B2: 'text-blue-400   bg-blue-500/20    border-blue-500/40',
  C1: 'text-purple-300 bg-purple-500/15  border-purple-500/30',
  C2: 'text-purple-400 bg-purple-500/20  border-purple-500/40',
}

// ─── Edit form (existing words) ───────────────────────────────────────────────

interface EditFormState {
  translation: string
  pronunciation: string
  ipa: string
  partOfSpeech: string
  cefrLevel: string
  difficulty: string
  categoryId: string
  notes: string
  personalMemory: string
  examples: string
  synonyms: string
  antonyms: string
}

function toEditForm(detail: { translation: string; pronunciation: string | null; ipa: string | null; partOfSpeech: PartOfSpeech | null; cefrLevel: CefrLevel | null; difficulty: number; category: { id: number } | null; notes: string | null; personalMemory: string | null; examples: string[]; synonyms: string[]; antonyms: string[] }): EditFormState {
  return {
    translation:   detail.translation,
    pronunciation: detail.pronunciation ?? '',
    ipa:           detail.ipa ?? '',
    partOfSpeech:  detail.partOfSpeech ?? '',
    cefrLevel:     detail.cefrLevel ?? '',
    difficulty:    String(detail.difficulty),
    categoryId:    String(detail.category?.id ?? ''),
    notes:         detail.notes ?? '',
    personalMemory: detail.personalMemory ?? '',
    examples:      detail.examples.join('\n'),
    synonyms:      detail.synonyms.join(', '),
    antonyms:      detail.antonyms.join(', '),
  }
}

function splitLines(s: string): string[] { return s.split('\n').map((l) => l.trim()).filter(Boolean) }
function splitCommas(s: string): string[] { return s.split(',').map((l) => l.trim()).filter(Boolean) }

// ─── Add modal (2-step AI flow) ───────────────────────────────────────────────

function AddWordModal({ onClose }: { onClose: () => void }) {
  const toast = useToast()
  const [wordInput, setWordInput] = useState('')
  const [details, setDetails]     = useState<WordDetails | null>(null)
  const [personalMemory, setPersonalMemory] = useState('')

  const enrichMut = useEnrichVocabulary()
  const createMut = useCreateVocabulary()

  async function handleSearch() {
    const word = wordInput.trim()
    if (!word) return
    try {
      const result = await enrichMut.mutateAsync(word)
      setDetails(result)
      setPersonalMemory('')
    } catch (e: any) {
      toast.error(e.message ?? 'Erro ao consultar palavra.')
    }
  }

  async function handleSave() {
    if (!details) return
    const payload: CreateVocabularyRequest = {
      word:              details.word,
      translation:       details.translation,
      pronunciation:     details.pronunciation ?? undefined,
      ipa:               details.ipa ?? undefined,
      partOfSpeech:      details.partOfSpeech as PartOfSpeech | undefined,
      cefrLevel:         details.cefrLevel as CefrLevel | undefined,
      difficulty:        details.difficulty ?? undefined,
      meaning:           details.meaning ?? undefined,
      personalMemory:    personalMemory.trim() || undefined,
      examples:          details.examples,
      synonyms:          details.synonyms,
      antonyms:          details.antonyms,
      collocations:      details.collocations,
      relatedPhrasalVerbs: details.relatedPhrasalVerbs,
      commonErrors:      details.commonErrors,
      usageTips:         details.usageTips,
    }
    try {
      await createMut.mutateAsync(payload)
      toast.success(`"${details.word}" adicionada ao seu vocabulário!`)
      onClose()
    } catch (e: any) {
      toast.error(e.message ?? 'Erro ao salvar palavra.')
    }
  }

  const searching = enrichMut.isPending
  const saving    = createMut.isPending

  return (
    <Modal
      open
      onClose={onClose}
      title="Adicionar palavra"
      size="lg"
      footer={
        details ? (
          <>
            <Button variant="secondary" size="sm" onClick={() => setDetails(null)} disabled={saving}>
              Buscar outra
            </Button>
            <Button size="sm" onClick={handleSave} loading={saving}>
              Salvar no vocabulário
            </Button>
          </>
        ) : (
          <>
            <Button variant="secondary" size="sm" onClick={onClose}>Cancelar</Button>
            <Button size="sm" onClick={handleSearch} loading={searching} disabled={!wordInput.trim()}>
              <Sparkles className="h-4 w-4" /> Buscar com IA
            </Button>
          </>
        )
      }
    >
      {!details ? (
        <div className="space-y-4">
          <p className="text-sm text-slate-400">
            Digite a palavra em inglês e a IA irá preencher automaticamente a tradução, pronúncia, exemplos e muito mais.
          </p>
          <Input
            label="Palavra em inglês"
            value={wordInput}
            onChange={(e) => setWordInput(e.target.value)}
            onKeyDown={(e) => { if (e.key === 'Enter' && !searching) handleSearch() }}
            placeholder="e.g. serendipity"
            autoFocus
          />
        </div>
      ) : (
        <div className="space-y-4 overflow-y-auto max-h-[65vh] pr-1">
          {/* Already exists warning */}
          {details.alreadyExists && (
            <div className="flex items-center gap-2 rounded-lg border border-amber-500/30 bg-amber-500/10 p-3 text-sm text-amber-300">
              <AlertCircle className="h-4 w-4 flex-shrink-0" />
              Esta palavra já está no seu vocabulário.
            </div>
          )}

          {/* Header */}
          <div className="flex items-start justify-between gap-4">
            <div>
              <h3 className="font-mono text-2xl font-bold text-slate-100">{details.word}</h3>
              <p className="text-slate-400 mt-0.5">{details.translation}</p>
              {(details.pronunciation || details.ipa) && (
                <p className="text-sm text-slate-500 mt-1 font-mono">
                  {details.pronunciation && <span>{details.pronunciation}</span>}
                  {details.ipa && <span className="ml-2">{details.ipa}</span>}
                </p>
              )}
            </div>
            <div className="flex flex-col gap-1 items-end flex-shrink-0">
              {details.cefrLevel && (
                <span className={cn('inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium',
                  CEFR_BADGE[(details.cefrLevel as CefrLevel)] ?? 'text-slate-400 bg-slate-500/15 border-slate-500/30')}>
                  {details.cefrLevel}
                </span>
              )}
              {details.partOfSpeech && (
                <Badge>{details.partOfSpeech.toLowerCase().replace('_', ' ')}</Badge>
              )}
            </div>
          </div>

          {/* Meaning */}
          {details.meaning && (
            <div>
              <p className="text-xs font-medium text-slate-500 uppercase tracking-wide mb-1">Significado</p>
              <p className="text-sm text-slate-300">{details.meaning}</p>
            </div>
          )}

          {/* Examples */}
          {details.examples.length > 0 && (
            <div>
              <p className="text-xs font-medium text-slate-500 uppercase tracking-wide mb-1">Como usar</p>
              <ul className="space-y-1">
                {details.examples.map((ex, i) => (
                  <li key={i} className="text-sm text-slate-300 pl-3 border-l-2 border-brand/40">{ex}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Synonyms / Antonyms */}
          {(details.synonyms.length > 0 || details.antonyms.length > 0) && (
            <div className="grid grid-cols-2 gap-3">
              {details.synonyms.length > 0 && (
                <div>
                  <p className="text-xs font-medium text-slate-500 uppercase tracking-wide mb-1">Sinônimos</p>
                  <p className="text-sm text-slate-400">{details.synonyms.join(', ')}</p>
                </div>
              )}
              {details.antonyms.length > 0 && (
                <div>
                  <p className="text-xs font-medium text-slate-500 uppercase tracking-wide mb-1">Antônimos</p>
                  <p className="text-sm text-slate-400">{details.antonyms.join(', ')}</p>
                </div>
              )}
            </div>
          )}

          {/* Common errors */}
          {details.commonErrors.length > 0 && (
            <div>
              <p className="text-xs font-medium text-slate-500 uppercase tracking-wide mb-1">Erros comuns</p>
              <ul className="space-y-0.5">
                {details.commonErrors.map((err, i) => (
                  <li key={i} className="text-sm text-red-300">{err}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Usage tips */}
          {details.usageTips.length > 0 && (
            <div>
              <p className="text-xs font-medium text-slate-500 uppercase tracking-wide mb-1">Dicas de uso</p>
              <ul className="space-y-0.5">
                {details.usageTips.map((tip, i) => (
                  <li key={i} className="text-sm text-slate-400">{tip}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Personal memory */}
          <div className="border-t border-border pt-4">
            <Textarea
              label="Minha forma de lembrar (opcional)"
              value={personalMemory}
              onChange={(e) => setPersonalMemory(e.target.value)}
              placeholder="Escreva uma associação, história ou dica pessoal para memorizar esta palavra..."
              rows={3}
            />
          </div>
        </div>
      )}
    </Modal>
  )
}

// ─── Edit modal (existing words) ──────────────────────────────────────────────

function EditWordModal({ editId, onClose }: { editId: number; onClose: () => void }) {
  const toast = useToast()
  const { data: detail, isLoading: loadingDetail } = useVocabularyDetail(editId)
  const { data: categories = [] } = useCategories()
  const updateMut = useUpdateVocabulary()

  const [form, setForm] = useState<EditFormState | null>(null)
  const [synced, setSynced] = useState(false)

  if (detail && !synced) {
    setForm(toEditForm(detail))
    setSynced(true)
  }

  function set(key: keyof EditFormState) {
    return (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
      setForm((f) => f ? { ...f, [key]: e.target.value } : f)
    }
  }

  async function handleSubmit() {
    if (!form) return
    const payload: UpdateVocabularyRequest = {
      translation:    form.translation.trim() || undefined,
      pronunciation:  form.pronunciation.trim() || undefined,
      ipa:            form.ipa.trim() || undefined,
      partOfSpeech:   (form.partOfSpeech as PartOfSpeech) || undefined,
      cefrLevel:      (form.cefrLevel as CefrLevel) || undefined,
      difficulty:     form.difficulty ? Number(form.difficulty) : undefined,
      categoryId:     form.categoryId ? Number(form.categoryId) : undefined,
      notes:          form.notes.trim() || undefined,
      personalMemory: form.personalMemory.trim() || undefined,
      examples:       splitLines(form.examples),
      synonyms:       splitCommas(form.synonyms),
      antonyms:       splitCommas(form.antonyms),
    }
    try {
      await updateMut.mutateAsync({ id: editId, data: payload })
      toast.success('Palavra atualizada com sucesso!')
      onClose()
    } catch (e: any) {
      toast.error(e.message ?? 'Erro ao salvar.')
    }
  }

  const catOptions = categories.map((c) => ({ value: c.id, label: c.name }))
  const saving = updateMut.isPending

  return (
    <Modal
      open
      onClose={onClose}
      title="Editar Palavra"
      size="lg"
      footer={
        <>
          <Button variant="secondary" size="sm" onClick={onClose} disabled={saving}>Cancelar</Button>
          <Button size="sm" onClick={handleSubmit} loading={saving}>Salvar alterações</Button>
        </>
      }
    >
      {loadingDetail || !form ? (
        <div className="flex justify-center py-8"><Spinner /></div>
      ) : (
        <div className="space-y-4 overflow-y-auto max-h-[60vh] pr-1">
          <Input label="Tradução" value={form.translation} onChange={set('translation')} />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Pronúncia" value={form.pronunciation} onChange={set('pronunciation')} />
            <Input label="IPA" value={form.ipa} onChange={set('ipa')} className="font-mono" />
          </div>
          <div className="grid grid-cols-3 gap-3">
            <Select label="Classe gramatical" value={form.partOfSpeech} onChange={set('partOfSpeech')}
              options={POS_OPTIONS} placeholder="Selecionar" />
            <Select label="Nível CEFR" value={form.cefrLevel} onChange={set('cefrLevel')}
              options={CEFR_OPTIONS} placeholder="Selecionar" />
            <Select label="Dificuldade" value={form.difficulty} onChange={set('difficulty')}
              options={DIFFICULTY_OPTIONS} />
          </div>
          <Select label="Categoria" value={form.categoryId} onChange={set('categoryId')}
            options={catOptions} placeholder="Sem categoria" />
          <Textarea label="Minha forma de lembrar" value={form.personalMemory} onChange={set('personalMemory')}
            placeholder="Uma dica pessoal para memorizar esta palavra..." rows={2} />
          <Textarea label="Notas" value={form.notes} onChange={set('notes')}
            placeholder="Observações, contexto, dicas de uso..." rows={2} />
          <Textarea label="Exemplos (um por linha)" value={form.examples} onChange={set('examples')} rows={3} />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Sinônimos (vírgula)" value={form.synonyms} onChange={set('synonyms')} />
            <Input label="Antônimos (vírgula)" value={form.antonyms} onChange={set('antonyms')} />
          </div>
        </div>
      )}
    </Modal>
  )
}

// ─── Word card ────────────────────────────────────────────────────────────────

interface WordCardProps {
  word: VocabularySummary
  onEdit: (id: number) => void
  onDelete: (id: number, label: string) => void
}

function WordCard({ word, onEdit, onDelete }: WordCardProps) {
  const navigate = useNavigate()
  const mastery = word.masteryLevel ?? 0
  return (
    <Card
      className="flex flex-col gap-3 p-4 group cursor-pointer hover:border-brand/40 transition-colors"
      onClick={() => navigate(`/vocabulary/${word.id}`)}
    >
      <div className="flex items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="font-mono text-base font-semibold text-slate-100 truncate">{word.word}</p>
          <p className="text-sm text-slate-400 truncate">{word.translation}</p>
        </div>
        <div className="flex gap-1 opacity-100 transition-opacity flex-shrink-0 md:opacity-0 md:group-hover:opacity-100">
          <button
            onClick={(e) => { e.stopPropagation(); onEdit(word.id) }}
            className="rounded p-1.5 text-slate-500 hover:bg-surface-hover hover:text-slate-300 transition-colors"
            aria-label="Editar"
          >
            <Pencil className="h-3.5 w-3.5" />
          </button>
          <button
            onClick={(e) => { e.stopPropagation(); onDelete(word.id, word.word) }}
            className="rounded p-1.5 text-slate-500 hover:bg-red-500/15 hover:text-red-400 transition-colors"
            aria-label="Excluir"
          >
            <Trash2 className="h-3.5 w-3.5" />
          </button>
        </div>
      </div>

      <div className="flex flex-wrap gap-1.5">
        {word.cefrLevel && (
          <span className={cn('inline-flex items-center rounded-full border px-2 py-0.5 text-xs font-medium',
            CEFR_BADGE[word.cefrLevel])}>
            {word.cefrLevel}
          </span>
        )}
        {word.partOfSpeech && (
          <Badge>{word.partOfSpeech.toLowerCase().replace('_', ' ')}</Badge>
        )}
        {word.categoryName && (
          <Badge variant="brand">{word.categoryName}</Badge>
        )}
      </div>

      <div className="space-y-1">
        <div className="flex justify-between text-xs text-slate-500">
          <span>Domínio</span>
          <span>{formatPercent(mastery)}</span>
        </div>
        <MasteryBar level={mastery} size="sm" />
      </div>

      {word.nextReviewDate && (
        <p className="text-[11px] text-slate-600">
          Próxima revisão: {formatRelativeDate(word.nextReviewDate)}
        </p>
      )}
    </Card>
  )
}

// ─── Pagination ───────────────────────────────────────────────────────────────

function Pagination({ current, total, onChange }: { current: number; total: number; onChange: (p: number) => void }) {
  if (total <= 1) return null
  return (
    <div className="flex items-center justify-center gap-2">
      <Button variant="ghost" size="sm" disabled={current === 0} onClick={() => onChange(current - 1)} className="h-8 w-8 p-0">
        <ChevronLeft className="h-4 w-4" />
      </Button>
      <span className="text-sm text-slate-400">{current + 1} / {total}</span>
      <Button variant="ghost" size="sm" disabled={current === total - 1} onClick={() => onChange(current + 1)} className="h-8 w-8 p-0">
        <ChevronRight className="h-4 w-4" />
      </Button>
    </div>
  )
}

// ─── Main page ────────────────────────────────────────────────────────────────

export function Vocabulary() {
  const toast = useToast()

  const [page, setPage]     = useState(0)
  const [search, setSearch] = useState('')
  const debouncedSearch     = useDebounce(search, 350)

  const [addOpen, setAddOpen]   = useState(false)
  const [editId, setEditId]     = useState<number | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<{ id: number; label: string } | null>(null)

  const { data, isLoading, isError } = useVocabularyList(page, debouncedSearch)
  const deleteMut = useDeleteVocabulary()

  async function handleDelete() {
    if (!deleteTarget) return
    try {
      await deleteMut.mutateAsync(deleteTarget.id)
      toast.success(`"${deleteTarget.label}" removida.`)
    } catch (e: any) {
      toast.error(e.message ?? 'Erro ao excluir palavra.')
    } finally {
      setDeleteTarget(null)
    }
  }

  return (
    <div className="space-y-5 animate-fade-in">
      {/* Top bar */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="relative max-w-xs w-full">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-500 pointer-events-none" />
          <input
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0) }}
            placeholder="Buscar palavras..."
            className={cn(
              'h-10 w-full rounded-lg border border-border bg-surface-elevated',
              'pl-9 pr-8 text-sm text-slate-100 placeholder:text-slate-500',
              'focus-ring transition-colors',
            )}
          />
          {search && (
            <button onClick={() => setSearch('')} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300">
              <X className="h-3.5 w-3.5" />
            </button>
          )}
        </div>
        <Button onClick={() => setAddOpen(true)} className="flex-shrink-0">
          <Plus className="h-4 w-4" /> Adicionar palavra
        </Button>
      </div>

      {/* Summary */}
      {data && (
        <p className="text-sm text-slate-500">
          {data.totalElements === 0
            ? 'Nenhuma palavra encontrada.'
            : `${data.totalElements} palavra${data.totalElements !== 1 ? 's' : ''} encontrada${data.totalElements !== 1 ? 's' : ''}`}
        </p>
      )}

      {/* Content */}
      {isLoading && <div className="flex justify-center py-20"><Spinner size="lg" /></div>}
      {isError  && <div className="py-20 text-center text-slate-400">Erro ao carregar vocabulário.</div>}

      {data && data.content.length === 0 && !isLoading && (
        <EmptyState
          icon={BookOpen}
          title={search ? 'Nenhum resultado encontrado' : 'Seu vocabulário está vazio'}
          description={search ? `Nenhuma palavra corresponde a "${search}".` : 'Comece adicionando sua primeira palavra em inglês.'}
          action={!search ? <Button onClick={() => setAddOpen(true)}><Plus className="h-4 w-4" /> Adicionar palavra</Button> : undefined}
        />
      )}

      {data && data.content.length > 0 && (
        <>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {data.content.map((word) => (
              <WordCard
                key={word.id}
                word={word}
                onEdit={(id) => setEditId(id)}
                onDelete={(id, label) => setDeleteTarget({ id, label })}
              />
            ))}
          </div>
          <Pagination current={page} total={data.totalPages} onChange={setPage} />
        </>
      )}

      {/* Add modal (AI 2-step) */}
      {addOpen && <AddWordModal onClose={() => setAddOpen(false)} />}

      {/* Edit modal */}
      {editId != null && <EditWordModal editId={editId} onClose={() => setEditId(null)} />}

      {/* Delete confirm */}
      <ConfirmDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Excluir palavra"
        message={`Tem certeza que deseja excluir "${deleteTarget?.label}"? Esta ação não pode ser desfeita.`}
        confirmLabel="Excluir"
        loading={deleteMut.isPending}
      />
    </div>
  )
}
