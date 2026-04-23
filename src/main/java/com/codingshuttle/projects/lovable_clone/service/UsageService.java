package com.codingshuttle.projects.lovable_clone.service;

import com.codingshuttle.projects.lovable_clone.dto.subscription.PlanLimitsResponse;
import com.codingshuttle.projects.lovable_clone.dto.subscription.UsageTodayResponse;

public interface UsageService {

    void recordTokenUsage(Long userId, int actualTokens);

    void checkDailyTokensUsage();
}
