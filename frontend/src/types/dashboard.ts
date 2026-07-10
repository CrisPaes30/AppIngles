export interface DailyProgress {
  date: string
  wordsReviewed: number
  exercisesCompleted: number
  studyMinutes: number
}

export interface TopMistakeWord {
  wordId: number
  word: string
  translation: string
  partOfSpeech: string | null
  incorrectCount: number
  correctCount: number
  totalReviews: number
  accuracyPct: number
}

export interface DashboardData {
  totalWords: number
  learnedWords: number
  learningWords: number
  weakWords: number
  wordsToReviewToday: number
  streakDays: number
  totalStudyMinutes: number
  averageMastery: number
  weeklyChart: DailyProgress[]

  // Métricas avançadas
  newWordsThisWeek: number
  wordsReviewedThisWeek: number
  overallAccuracyPct: number
  weakestPartOfSpeech: string | null
  topMistakeWords: TopMistakeWord[]
}
