package com.englishmemory.service;

import com.englishmemory.dto.response.ProgressSummaryResponse;

public interface ProgressService {

    ProgressSummaryResponse getSummary(Long userId);
}
