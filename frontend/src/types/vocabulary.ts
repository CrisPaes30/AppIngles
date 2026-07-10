export type CefrLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1' | 'C2'

export type PartOfSpeech =
  | 'NOUN' | 'VERB' | 'ADJECTIVE' | 'ADVERB' | 'PREPOSITION'
  | 'CONJUNCTION' | 'INTERJECTION' | 'PHRASAL_VERB' | 'EXPRESSION' | 'OTHER'

export interface Category {
  id: number
  name: string
  description: string | null
  color: string
  icon: string
  wordCount: number
  createdAt: string
}

export interface ReviewSchedule {
  id: number
  easeFactor: number
  repetitions: number
  intervalDays: number
  nextReviewDate: string
  lastReviewedAt: string | null
  correctCount: number
  incorrectCount: number
  accuracyPercentage: number
}

export interface VocabularySummary {
  id: number
  word: string
  translation: string
  cefrLevel: CefrLevel | null
  partOfSpeech: PartOfSpeech | null
  difficulty: number
  categoryName: string | null
  masteryLevel: number | null
  nextReviewDate: string | null
}

export interface VocabularyWord {
  id: number
  word: string
  translation: string
  pronunciation: string | null
  ipa: string | null
  partOfSpeech: PartOfSpeech | null
  cefrLevel: CefrLevel | null
  difficulty: number
  meaning: string | null
  notes: string | null
  personalMemory: string | null
  examples: string[]
  synonyms: string[]
  antonyms: string[]
  collocations: string[]
  relatedPhrasalVerbs: string[]
  commonErrors: string[]
  usageTips: string[]
  imageUrl: string | null
  audioUrl: string | null
  category: Category | null
  reviewSchedule: ReviewSchedule | null
  createdAt: string
  updatedAt: string
}

export interface WordDetails {
  word: string
  translation: string
  pronunciation: string | null
  ipa: string | null
  meaning: string | null
  partOfSpeech: string | null
  cefrLevel: string | null
  difficulty: number | null
  examples: string[]
  synonyms: string[]
  antonyms: string[]
  collocations: string[]
  relatedPhrasalVerbs: string[]
  commonErrors: string[]
  usageTips: string[]
  alreadyExists: boolean
}

export interface CreateVocabularyRequest {
  word: string
  translation: string
  pronunciation?: string
  ipa?: string
  partOfSpeech?: PartOfSpeech
  categoryId?: number
  cefrLevel?: CefrLevel
  difficulty?: number
  meaning?: string
  notes?: string
  personalMemory?: string
  examples?: string[]
  synonyms?: string[]
  antonyms?: string[]
  collocations?: string[]
  relatedPhrasalVerbs?: string[]
  commonErrors?: string[]
  usageTips?: string[]
}

export interface UpdateVocabularyRequest extends Partial<CreateVocabularyRequest> {}
