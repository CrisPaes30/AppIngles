import { useState } from 'react'
import {
  Plus, Pencil, Trash2, FolderOpen,
  BookOpen, Star, Heart, Zap, Globe, Briefcase, Coffee,
  Music, Film, Code, Home, Plane, Sun, Smile, Award,
  Trophy, Target, Rocket, Tag, Lightbulb, Brain, Pen, MessageSquare,
} from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import {
  useCategories,
  useCreateCategory,
  useUpdateCategory,
  useDeleteCategory,
} from '@/hooks/useCategories'
import { useToast }      from '@/contexts/ToastContext'
import { Button }        from '@/components/ui/Button'
import { Input }         from '@/components/ui/Input'
import { Textarea }      from '@/components/ui/Textarea'
import { Card }          from '@/components/ui/Card'
import { Spinner }       from '@/components/ui/Spinner'
import { Modal }         from '@/components/ui/Modal'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { EmptyState }    from '@/components/ui/EmptyState'
import { cn } from '@/utils/cn'
import type { Category } from '@/types/vocabulary'
import type { CreateCategoryRequest } from '@/services/category.service'

// ─── Constants ────────────────────────────────────────────────────────────────

const COLOR_PALETTE = [
  '#6366f1', '#8b5cf6', '#a855f7', '#ec4899',
  '#ef4444', '#f97316', '#f59e0b', '#eab308',
  '#84cc16', '#10b981', '#14b8a6', '#06b6d4',
  '#3b82f6', '#64748b',
]

const ICONS: { key: string; icon: LucideIcon; label: string }[] = [
  { key: 'book',      icon: BookOpen,      label: 'Livro'       },
  { key: 'star',      icon: Star,          label: 'Estrela'     },
  { key: 'heart',     icon: Heart,         label: 'Coração'     },
  { key: 'zap',       icon: Zap,           label: 'Energia'     },
  { key: 'globe',     icon: Globe,         label: 'Mundo'       },
  { key: 'briefcase', icon: Briefcase,     label: 'Trabalho'    },
  { key: 'coffee',    icon: Coffee,        label: 'Café'        },
  { key: 'music',     icon: Music,         label: 'Música'      },
  { key: 'film',      icon: Film,          label: 'Cinema'      },
  { key: 'code',      icon: Code,          label: 'Tech'        },
  { key: 'home',      icon: Home,          label: 'Casa'        },
  { key: 'plane',     icon: Plane,         label: 'Viagem'      },
  { key: 'sun',       icon: Sun,           label: 'Natureza'    },
  { key: 'smile',     icon: Smile,         label: 'Social'      },
  { key: 'award',     icon: Award,         label: 'Conquista'   },
  { key: 'trophy',    icon: Trophy,        label: 'Troféu'      },
  { key: 'target',    icon: Target,        label: 'Objetivo'    },
  { key: 'rocket',    icon: Rocket,        label: 'Progresso'   },
  { key: 'tag',       icon: Tag,           label: 'Etiqueta'    },
  { key: 'lightbulb', icon: Lightbulb,     label: 'Ideia'       },
  { key: 'brain',     icon: Brain,         label: 'Memória'     },
  { key: 'pen',       icon: Pen,           label: 'Escrita'     },
  { key: 'message',   icon: MessageSquare, label: 'Diálogo'     },
]

const ICON_MAP = Object.fromEntries(ICONS.map(({ key, icon }) => [key, icon])) as Record<string, LucideIcon>

function getCategoryIcon(name: string): LucideIcon {
  return ICON_MAP[name] ?? BookOpen
}

// ─── Form state ───────────────────────────────────────────────────────────────

interface FormState {
  name: string
  description: string
  color: string
  icon: string
}

const DEFAULT_FORM: FormState = {
  name:        '',
  description: '',
  color:       '#6366f1',
  icon:        'book',
}

// ─── Color picker ─────────────────────────────────────────────────────────────

function ColorPicker({ value, onChange }: { value: string; onChange: (c: string) => void }) {
  return (
    <div className="space-y-1.5">
      <p className="text-sm font-medium text-slate-300">Cor</p>
      <div className="flex flex-wrap gap-2">
        {COLOR_PALETTE.map((color) => (
          <button
            key={color}
            type="button"
            onClick={() => onChange(color)}
            title={color}
            className={cn(
              'h-7 w-7 rounded-full border-2 transition-transform hover:scale-110',
              value === color ? 'border-white scale-110' : 'border-transparent',
            )}
            style={{ backgroundColor: color }}
          />
        ))}
      </div>
    </div>
  )
}

// ─── Icon picker ──────────────────────────────────────────────────────────────

function IconPicker({
  value,
  color,
  onChange,
}: {
  value: string
  color: string
  onChange: (k: string) => void
}) {
  return (
    <div className="space-y-1.5">
      <p className="text-sm font-medium text-slate-300">Ícone</p>
      <div className="grid grid-cols-8 gap-1.5 sm:grid-cols-12">
        {ICONS.map(({ key, icon: Icon, label }) => (
          <button
            key={key}
            type="button"
            onClick={() => onChange(key)}
            title={label}
            className={cn(
              'flex h-8 w-8 items-center justify-center rounded-lg border transition-all',
              value === key
                ? 'border-2 bg-opacity-20'
                : 'border-border bg-surface-elevated hover:bg-surface-hover',
            )}
            style={
              value === key
                ? { borderColor: color, backgroundColor: color + '25' }
                : undefined
            }
          >
            <Icon
              className="h-4 w-4"
              style={{ color: value === key ? color : undefined }}
            />
          </button>
        ))}
      </div>
    </div>
  )
}

// ─── Category form ────────────────────────────────────────────────────────────

interface CategoryFormProps {
  form: FormState
  onChange: (f: FormState) => void
}

function CategoryForm({ form, onChange }: CategoryFormProps) {
  const set = (key: keyof FormState) => (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => onChange({ ...form, [key]: e.target.value })

  return (
    <div className="space-y-5">
      <Input
        label="Nome *"
        value={form.name}
        onChange={set('name')}
        placeholder="ex: Phrasal Verbs"
        autoFocus
      />
      <Textarea
        label="Descrição"
        value={form.description}
        onChange={set('description')}
        placeholder="Opcional — descreva o que esta categoria contém"
        rows={2}
      />
      <ColorPicker value={form.color} onChange={(c) => onChange({ ...form, color: c })} />
      <IconPicker
        value={form.icon}
        color={form.color}
        onChange={(k) => onChange({ ...form, icon: k })}
      />
    </div>
  )
}

// ─── Add / Edit modal ─────────────────────────────────────────────────────────

interface CategoryModalProps {
  initial: Partial<Category> | null
  onClose: () => void
}

function CategoryModal({ initial, onClose }: CategoryModalProps) {
  const toast    = useToast()
  const isEdit   = initial != null && initial.id != null
  const createMut = useCreateCategory()
  const updateMut = useUpdateCategory()
  const loading   = createMut.isPending || updateMut.isPending

  const [form, setForm] = useState<FormState>(
    isEdit && initial
      ? {
          name:        initial.name        ?? '',
          description: initial.description ?? '',
          color:       initial.color       ?? '#6366f1',
          icon:        initial.icon        ?? 'book',
        }
      : DEFAULT_FORM,
  )

  async function handleSubmit() {
    if (!form.name.trim()) {
      toast.error('O nome da categoria é obrigatório.')
      return
    }
    const payload: CreateCategoryRequest = {
      name:        form.name.trim(),
      description: form.description.trim(),
      color:       form.color,
      icon:        form.icon,
    }
    try {
      if (isEdit && initial?.id) {
        await updateMut.mutateAsync({ id: initial.id, data: payload })
        toast.success('Categoria atualizada!')
      } else {
        await createMut.mutateAsync(payload)
        toast.success('Categoria criada!')
      }
      onClose()
    } catch (e: any) {
      toast.error(e.message ?? 'Erro ao salvar categoria.')
    }
  }

  return (
    <Modal
      open
      onClose={onClose}
      title={isEdit ? 'Editar Categoria' : 'Nova Categoria'}
      size="md"
      footer={
        <>
          <Button variant="secondary" size="sm" onClick={onClose} disabled={loading}>
            Cancelar
          </Button>
          <Button size="sm" onClick={handleSubmit} loading={loading}>
            {isEdit ? 'Salvar alterações' : 'Criar categoria'}
          </Button>
        </>
      }
    >
      <CategoryForm form={form} onChange={setForm} />
    </Modal>
  )
}

// ─── Category card ────────────────────────────────────────────────────────────

interface CategoryCardProps {
  category: Category
  onEdit:   (cat: Category) => void
  onDelete: (cat: Category) => void
}

function CategoryCard({ category, onEdit, onDelete }: CategoryCardProps) {
  const Icon = getCategoryIcon(category.icon)
  const hex  = category.color

  return (
    <Card className="group relative flex flex-col gap-3 p-5 overflow-hidden">
      {/* Color accent bar */}
      <div
        className="absolute left-0 inset-y-0 w-1 rounded-l-xl"
        style={{ backgroundColor: hex }}
      />

      {/* Top row */}
      <div className="flex items-start justify-between gap-2 pl-2">
        {/* Icon + name */}
        <div className="flex items-center gap-3 min-w-0">
          <div
            className="flex h-10 w-10 flex-shrink-0 items-center justify-center rounded-xl"
            style={{ backgroundColor: hex + '22' }}
          >
            <Icon className="h-5 w-5" style={{ color: hex }} />
          </div>
          <div className="min-w-0">
            <p className="font-semibold text-slate-100 truncate">{category.name}</p>
            {category.description && (
              <p className="text-xs text-slate-500 truncate mt-0.5">{category.description}</p>
            )}
          </div>
        </div>

        {/* Actions — visible on hover */}
        <div className="flex gap-1 flex-shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
          <button
            onClick={() => onEdit(category)}
            className="rounded p-1.5 text-slate-500 hover:bg-surface-hover hover:text-slate-300 transition-colors"
            aria-label="Editar"
          >
            <Pencil className="h-3.5 w-3.5" />
          </button>
          <button
            onClick={() => onDelete(category)}
            className="rounded p-1.5 text-slate-500 hover:bg-red-500/15 hover:text-red-400 transition-colors"
            aria-label="Excluir"
          >
            <Trash2 className="h-3.5 w-3.5" />
          </button>
        </div>
      </div>

      {/* Word count */}
      <div className="pl-2">
        <span
          className="inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium"
          style={{ backgroundColor: hex + '18', color: hex }}
        >
          {category.wordCount} {category.wordCount === 1 ? 'palavra' : 'palavras'}
        </span>
      </div>
    </Card>
  )
}

// ─── Main ─────────────────────────────────────────────────────────────────────

export function Categories() {
  const toast = useToast()
  const { data: categories = [], isLoading } = useCategories()
  const deleteMut = useDeleteCategory()

  const [modalTarget,  setModalTarget]  = useState<Partial<Category> | null | 'new'>(null)
  const [deleteTarget, setDeleteTarget] = useState<Category | null>(null)

  function openNew()            { setModalTarget('new') }
  function openEdit(cat: Category) { setModalTarget(cat) }
  function closeModal()         { setModalTarget(null) }

  async function handleDelete() {
    if (!deleteTarget) return
    try {
      await deleteMut.mutateAsync(deleteTarget.id)
      toast.success(`"${deleteTarget.name}" removida.`)
    } catch (e: any) {
      toast.error(e.message ?? 'Erro ao excluir categoria.')
    } finally {
      setDeleteTarget(null)
    }
  }

  const isModalOpen = modalTarget !== null
  const editPayload = modalTarget === 'new' ? null : modalTarget

  return (
    <div className="space-y-5 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <p className="text-sm text-slate-500">
          {isLoading ? '' : `${categories.length} categoria${categories.length !== 1 ? 's' : ''}`}
        </p>
        <Button onClick={openNew}>
          <Plus className="h-4 w-4" /> Nova categoria
        </Button>
      </div>

      {/* Content */}
      {isLoading && (
        <div className="flex justify-center py-20">
          <Spinner size="lg" />
        </div>
      )}

      {!isLoading && categories.length === 0 && (
        <EmptyState
          icon={FolderOpen}
          title="Nenhuma categoria ainda"
          description="Organize seu vocabulário criando categorias como Phrasal Verbs, Grammar ou Travel."
          action={
            <Button onClick={openNew}>
              <Plus className="h-4 w-4" /> Criar primeira categoria
            </Button>
          }
        />
      )}

      {!isLoading && categories.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map((cat) => (
            <CategoryCard
              key={cat.id}
              category={cat}
              onEdit={openEdit}
              onDelete={setDeleteTarget}
            />
          ))}
        </div>
      )}

      {/* Modal */}
      {isModalOpen && (
        <CategoryModal initial={editPayload} onClose={closeModal} />
      )}

      {/* Delete confirm */}
      <ConfirmDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Excluir categoria"
        message={`Tem certeza que deseja excluir "${deleteTarget?.name}"? As palavras vinculadas não serão excluídas.`}
        confirmLabel="Excluir"
        loading={deleteMut.isPending}
      />
    </div>
  )
}
