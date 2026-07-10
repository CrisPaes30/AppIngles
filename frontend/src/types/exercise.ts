export type ExerciseType =
  | 'MULTIPLE_CHOICE'
  | 'FILL_BLANK'
  | 'WORD_ORDER'
  | 'TRANSLATION'
  | 'TRUE_FALSE'
  | 'SENTENCE_BUILDING'

export const EXERCISE_TYPE_LABELS: Record<ExerciseType, string> = {
  MULTIPLE_CHOICE:   'Múltipla Escolha',
  FILL_BLANK:        'Completar Lacuna',
  WORD_ORDER:        'Ordenar Palavras',
  TRANSLATION:       'Tradução',
  TRUE_FALSE:        'Verdadeiro ou Falso',
  SENTENCE_BUILDING: 'Construção de Frase',
}

export interface Exercise {
  id: number
  type: ExerciseType
  question: string
  options: string[] | null
  vocabularyWordId: number | null
  createdAt: string
}

export interface GenerateExerciseRequest {
  vocabularyWordId?: number
  type?: ExerciseType
}

export interface AnswerExerciseRequest {
  answer: string
  timeSpentSeconds?: number
}

export interface ExerciseAnswer {
  exerciseId: number
  type: ExerciseType
  isCorrect: boolean
  userAnswer: string
  correctAnswer: string
  explanation: string
  masteryLevel: number | null
  timeSpentSeconds: number | null
}

export interface SentencePractice {
  id: number
  originalSentence: string
  correctedSentence: string
  aiFeedback: string
  grammarExplanation: string
  suggestedSentences: string[]
  newVocabularyFound: string[]
  score: number
  createdAt: string
}
