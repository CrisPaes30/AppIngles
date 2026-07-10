package com.englishmemory.service;

import com.englishmemory.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard(Long userId);
}
