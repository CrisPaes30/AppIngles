package com.englishmemory.util;

import com.englishmemory.util.Sm2Algorithm.Sm2Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Sm2Algorithm — testes do algoritmo de repetição espaçada")
class Sm2AlgorithmTest {

    @Nested
    @DisplayName("Falha (quality < 3)")
    class WhenFailed {

        @ParameterizedTest(name = "quality={0}")
        @ValueSource(ints = {0, 1, 2})
        @DisplayName("deve reiniciar repetitions para 0 e interval para 1 dia")
        void shouldResetOnFailure(int quality) {
            Sm2Result result = Sm2Algorithm.calculate(2.5, 5, 30, quality);

            assertThat(result.repetitions()).isEqualTo(0);
            assertThat(result.intervalDays()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Sucesso (quality >= 3)")
    class WhenSucceeded {

        @Test
        @DisplayName("primeira revisão: interval deve ser 1 dia")
        void firstRepetition_shouldSetInterval1() {
            Sm2Result result = Sm2Algorithm.calculate(2.5, 0, 1, 4);

            assertThat(result.repetitions()).isEqualTo(1);
            assertThat(result.intervalDays()).isEqualTo(1);
        }

        @Test
        @DisplayName("segunda revisão: interval deve ser 6 dias")
        void secondRepetition_shouldSetInterval6() {
            Sm2Result result = Sm2Algorithm.calculate(2.5, 1, 1, 4);

            assertThat(result.repetitions()).isEqualTo(2);
            assertThat(result.intervalDays()).isEqualTo(6);
        }

        @Test
        @DisplayName("terceira revisão em diante: interval = anterior * easeFactor")
        void thirdRepetition_shouldMultiplyByEaseFactor() {
            Sm2Result result = Sm2Algorithm.calculate(2.5, 2, 6, 4);

            assertThat(result.repetitions()).isEqualTo(3);
            assertThat(result.intervalDays()).isGreaterThan(6);
        }
    }

    @Nested
    @DisplayName("Ease Factor")
    class EaseFactor {

        @Test
        @DisplayName("resposta difícil (quality=3) deve reduzir ease factor")
        void hardResponse_shouldDecreaseEaseFactor() {
            Sm2Result result = Sm2Algorithm.calculate(2.5, 3, 10, 3);

            assertThat(result.easeFactor()).isLessThan(2.5);
        }

        @Test
        @DisplayName("resposta fácil (quality=5) deve aumentar ease factor")
        void easyResponse_shouldIncreaseEaseFactor() {
            Sm2Result result = Sm2Algorithm.calculate(2.5, 3, 10, 5);

            assertThat(result.easeFactor()).isGreaterThan(2.5);
        }

        @ParameterizedTest(name = "quality={0}")
        @ValueSource(ints = {0, 1, 2, 3})
        @DisplayName("ease factor nunca deve cair abaixo de 1.3")
        void shouldNeverDropBelow1_3(int quality) {
            Sm2Result result = Sm2Algorithm.calculate(1.3, 0, 1, quality);

            assertThat(result.easeFactor()).isGreaterThanOrEqualTo(1.3);
        }

        @Test
        @DisplayName("muitas respostas difíceis consecutivas não devem zerar o ease factor")
        void repeatedFailures_shouldNotZeroEaseFactor() {
            double ef = 2.5;
            int    reps = 0, interval = 1;

            for (int i = 0; i < 20; i++) {
                Sm2Result r = Sm2Algorithm.calculate(ef, reps, interval, 0);
                ef       = r.easeFactor();
                reps     = r.repetitions();
                interval = r.intervalDays();
            }

            assertThat(ef).isGreaterThanOrEqualTo(1.3);
        }
    }

    @Nested
    @DisplayName("Mastery Level")
    class MasteryLevel {

        @Test
        @DisplayName("sem revisões: mastery deve ser 0")
        void noReviews_masteryIsZero() {
            int mastery = Sm2Algorithm.calculateMastery(0, 0, 0);
            assertThat(mastery).isEqualTo(0);
        }

        @Test
        @DisplayName("100% de acerto com muitas repetições: mastery deve ser 100")
        void perfectAccuracyAndRepetitions_masteryIs100() {
            int mastery = Sm2Algorithm.calculateMastery(10, 0, 6);
            assertThat(mastery).isEqualTo(100);
        }

        @Test
        @DisplayName("mastery deve estar sempre entre 0 e 100")
        void masteryAlwaysInRange() {
            assertThat(Sm2Algorithm.calculateMastery(0, 10, 0)).isBetween(0, 100);
            assertThat(Sm2Algorithm.calculateMastery(5, 5, 3)).isBetween(0, 100);
            assertThat(Sm2Algorithm.calculateMastery(100, 0, 20)).isBetween(0, 100);
        }

        @Test
        @DisplayName("acerto parcial deve resultar em mastery proporcional")
        void partialAccuracy_shouldYieldProportionalMastery() {
            int mastery50 = Sm2Algorithm.calculateMastery(5, 5, 0);
            int mastery75 = Sm2Algorithm.calculateMastery(3, 1, 0);

            assertThat(mastery75).isGreaterThan(mastery50);
        }
    }

    @Test
    @DisplayName("quality inválido deve lançar IllegalArgumentException")
    void invalidQuality_shouldThrow() {
        assertThatThrownBy(() -> Sm2Algorithm.calculate(2.5, 0, 1, 6))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> Sm2Algorithm.calculate(2.5, 0, 1, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
