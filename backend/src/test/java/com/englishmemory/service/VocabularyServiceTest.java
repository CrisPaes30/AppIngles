package com.englishmemory.service;

import com.englishmemory.dto.request.CreateVocabularyRequest;
import com.englishmemory.dto.response.VocabularyResponse;
import com.englishmemory.entity.ReviewSchedule;
import com.englishmemory.entity.User;
import com.englishmemory.entity.VocabularyWord;
import com.englishmemory.exception.BusinessException;
import com.englishmemory.exception.ResourceNotFoundException;
import com.englishmemory.mapper.ReviewScheduleMapper;
import com.englishmemory.mapper.VocabularyMapper;
import com.englishmemory.repository.*;
import com.englishmemory.service.impl.VocabularyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VocabularyService — testes unitários")
class VocabularyServiceTest {

    @Mock private VocabularyWordRepository vocabularyRepository;
    @Mock private ReviewScheduleRepository reviewScheduleRepository;
    @Mock private ProgressRepository progressRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private VocabularyMapper vocabularyMapper;
    @Mock private ReviewScheduleMapper reviewScheduleMapper;

    @InjectMocks
    private VocabularyServiceImpl service;

    private static final Long USER_ID = 1L;

    private User defaultUser;
    private VocabularyWord defaultWord;

    @BeforeEach
    void setUp() {
        defaultUser = User.builder().name("Test User").email("test@test.com").build();

        defaultWord = new VocabularyWord();
        defaultWord.setWord("run");
        defaultWord.setTranslation("correr");
        defaultWord.setUser(defaultUser);
    }

    @Test
    @DisplayName("create — deve lançar BusinessException quando palavra já existe")
    void create_shouldThrowWhenWordAlreadyExists() {
        CreateVocabularyRequest request = new CreateVocabularyRequest();
        request.setWord("run");
        request.setTranslation("correr");

        when(vocabularyRepository.existsByWordAndUserIdAndActiveTrue("run", USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.create(USER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("run");

        verify(vocabularyRepository, never()).save(any());
    }

    @Test
    @DisplayName("create — deve criar palavra com ReviewSchedule e Progress iniciais")
    void create_shouldCreateWordWithScheduleAndProgress() {
        CreateVocabularyRequest request = new CreateVocabularyRequest();
        request.setWord("run");
        request.setTranslation("correr");

        when(vocabularyRepository.existsByWordAndUserIdAndActiveTrue("run", USER_ID)).thenReturn(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(defaultUser));
        when(vocabularyMapper.toEntity(request)).thenReturn(defaultWord);
        when(vocabularyRepository.save(defaultWord)).thenReturn(defaultWord);
        when(vocabularyMapper.toResponse(defaultWord)).thenReturn(new VocabularyResponse());
        when(reviewScheduleRepository.findByVocabularyWordIdAndActiveTrue(any())).thenReturn(Optional.empty());

        service.create(USER_ID, request);

        verify(reviewScheduleRepository, times(1)).save(any(ReviewSchedule.class));
        verify(progressRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("findById — deve lançar ResourceNotFoundException quando palavra não existe")
    void findById_shouldThrowWhenWordNotFound() {
        when(vocabularyRepository.findByIdAndUserIdAndActiveTrue(99L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(USER_ID, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("delete — deve realizar soft delete na palavra, schedule e progress")
    void delete_shouldSoftDeleteWordAndRelated() {
        defaultWord.setActive(true);

        when(vocabularyRepository.findByIdAndUserIdAndActiveTrue(1L, USER_ID))
                .thenReturn(Optional.of(defaultWord));
        when(vocabularyRepository.save(any())).thenReturn(defaultWord);
        when(reviewScheduleRepository.findByVocabularyWordIdAndActiveTrue(any()))
                .thenReturn(Optional.empty());
        when(progressRepository.findByUserIdAndVocabularyWordIdAndActiveTrue(any(), any()))
                .thenReturn(Optional.empty());

        service.delete(USER_ID, 1L);

        assertThat(defaultWord.getActive()).isFalse();
        verify(vocabularyRepository).save(defaultWord);
    }
}
