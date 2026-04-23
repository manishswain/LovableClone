package com.codingshuttle.projects.lovable_clone.service.impl;

import com.codingshuttle.projects.lovable_clone.dto.subscription.PlanResponse;
import com.codingshuttle.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.codingshuttle.projects.lovable_clone.entity.UsageLog;
import com.codingshuttle.projects.lovable_clone.repository.UsageLogRepository;
import com.codingshuttle.projects.lovable_clone.security.AuthUtil;
import com.codingshuttle.projects.lovable_clone.service.SubscriptionService;
import com.codingshuttle.projects.lovable_clone.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UsageServiceImpl implements UsageService {

    private final UsageLogRepository usageLogRepository;
    private final AuthUtil  authUtil;
    private final SubscriptionService  subscriptionService;

    @Override
    public void recordTokenUsage(Long userId, int actualTokens) {
        LocalDate today = LocalDate.now();

        UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId,today).orElseGet(()-> createNewDailyLog(userId, today));

        todayLog.setTokensUsed(todayLog.getTokensUsed()+actualTokens);
        usageLogRepository.save(todayLog);
    }

    @Override
    public void checkDailyTokensUsage() {
        long userId = authUtil.getCurrentUserId();
        SubscriptionResponse subscriptionResponse = subscriptionService.getCurrentSubscription();
        PlanResponse planResponse = subscriptionResponse.plan();

        LocalDate today = LocalDate.now();

        UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId,today)
                .orElseGet(()-> createNewDailyLog(userId, today));

        if(planResponse!=null && planResponse.unlimitedAi()) return; // No need to check limits for unlimited plans

        int currentUsage = todayLog.getTokensUsed();
        int limit = planResponse.maxTokensPerDay();

        if(currentUsage >= limit) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Daily token usage limit exceeded. Please upgrade your plan to increase the limit.");
        }
    }

    private UsageLog createNewDailyLog(Long userId, LocalDate today) {
        UsageLog usageLog = UsageLog.builder()
                .userId(userId)
                .date(today)
                .tokensUsed(0)
                .build();

        return usageLogRepository.save(usageLog);
    }
}
