package com.englishmemory.util;

/**
 * Implementação do algoritmo SM-2 (SuperMemo 2) para repetição espaçada.
 *
 * Qualidade da resposta (0–5):
 *   0 = Blackout completo
 *   1 = Errou, mas reconheceu a resposta ao ver
 *   2 = Errou, mas parecia fácil ao ver a resposta
 *   3 = Acertou com muita dificuldade
 *   4 = Acertou com hesitação
 *   5 = Acertou perfeitamente
 *
 * Qualidade < 3 é considerada falha — reinicia o ciclo de repetição.
 */
public final class Sm2Algorithm {

    public static final double EASE_FACTOR_DEFAULT = 2.5;
    public static final double EASE_FACTOR_MIN     = 1.3;
    public static final int    QUALITY_THRESHOLD   = 3;

    private Sm2Algorithm() {}

    /**
     * Calcula o novo estado SM-2 com base na qualidade da resposta.
     *
     * @param easeFactor   fator de facilidade atual (mínimo 1.3)
     * @param repetitions  número de repetições bem-sucedidas consecutivas
     * @param intervalDays intervalo atual em dias
     * @param quality      qualidade da resposta (0–5)
     * @return novo estado SM-2
     */
    public static Sm2Result calculate(double easeFactor, int repetitions, int intervalDays, int quality) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Qualidade deve estar entre 0 e 5.");
        }

        double newEaseFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
        newEaseFactor = Math.max(EASE_FACTOR_MIN, newEaseFactor);

        int newRepetitions;
        int newInterval;

        if (quality < QUALITY_THRESHOLD) {
            newRepetitions = 0;
            newInterval    = 1;
        } else {
            newRepetitions = repetitions + 1;
            newInterval = switch (repetitions) {
                case 0  -> 1;
                case 1  -> 6;
                default -> (int) Math.round(intervalDays * newEaseFactor);
            };
        }

        return new Sm2Result(newEaseFactor, newRepetitions, newInterval);
    }

    /**
     * Calcula o mastery level (0–100) baseado no histórico de revisões.
     *
     * A fórmula pondera a taxa de acerto (70%) com bônus por repetições
     * consecutivas bem-sucedidas (30%). Quanto mais repetições corretas, maior
     * o intervalo e, por consequência, maior a evidência de memorização.
     */
    public static int calculateMastery(int correctCount, int incorrectCount, int repetitions) {
        int total = correctCount + incorrectCount;
        if (total == 0) return 0;

        double accuracy      = (double) correctCount / total;
        int    repetitionCap = Math.min(repetitions * 5, 30);
        int    mastery       = (int) (accuracy * 70) + repetitionCap;

        return Math.min(100, Math.max(0, mastery));
    }

    public record Sm2Result(double easeFactor, int repetitions, int intervalDays) {}
}
