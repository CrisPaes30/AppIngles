import type { CefrLevel, PartOfSpeech } from './vocabulary'

export interface ReviewCard {
  vocabularyWordId: number
  word: string
  translation: string
  pronunciation: string | null
  ipa: string | null
  partOfSpeech: PartOfSpeech | null
  cefrLevel: CefrLevel | null
  examples: string[]
  synonyms: string[]
  categoryName: string | null
  repetitions: number
  intervalDays: number
  correctCount: number
  incorrectCount: number
  accuracyPercentage: number
  masteryLevel: number
  nextReviewDate: string
  lastReviewedAt: string | null
}

export interface ReviewAnswerRequest {
  quality: 0 | 1 | 2 | 3 | 4 | 5
}

export interface ReviewResult {
  vocabularyWordId: number
  word: string
  correct: boolean
  quality: number
  newRepetitions: number
  newIntervalDays: number
  newEaseFactor: number
  masteryLevel: number
  nextReviewDate: string
  feedbackMessage: string
}

export type ReviewQuality = 0 | 3 | 4 | 5

export const REVIEW_QUALITY_LABELS: Record<ReviewQuality, { label: string; color: string }> = {
  0: { label: 'Errei',    color: 'text-red-400'    },
  3: { label: 'Difícil',  color: 'text-orange-400' },
  4: { label: 'Bom',      color: 'text-blue-400'   },
  5: { label: 'Fácil',    color: 'text-emerald-400' },
}
