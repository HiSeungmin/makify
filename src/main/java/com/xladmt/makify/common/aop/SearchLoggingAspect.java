package com.xladmt.makify.common.aop;

import com.xladmt.makify.search.service.SearchKeywordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 검색 기능의 검색어를 로깅하고 Redis에 카운팅하는 AOP
 * 1. data.log에 검색어 기록
 * 2. Redis에 실시간 카운팅
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SearchLoggingAspect {

    private final SearchKeywordService searchKeywordService;
    private static final org.slf4j.Logger searchLogger = org.slf4j.LoggerFactory.getLogger("search");

    // ChallengeController의 searchChallenges 메서드 로깅
    @Around("execution(* com.xladmt.makify.challenge.controller.ChallengeApiController.searchChallenges(..))")
    public Object logSearchChallenges(ProceedingJoinPoint joinPoint) throws Throwable {
        // 파라미터 추출
        Object[] args = joinPoint.getArgs();
        String keyword = null;

        // searchChallenges(keyword, category, status, sortBy, page, size)
        // 첫 번째 파라미터가 keyword
        if (args.length > 0 && args[0] instanceof String) {
            keyword = (String) args[0];
        }

        // 검색어가 있으면 로깅 및 카운팅
        if (keyword != null && !keyword.trim().isEmpty()) {
            // data.log에 기록 (날짜 시분초 + 검색어)
            searchLogger.info("[SEARCH] keyword={}", keyword.trim());

        }

        return joinPoint.proceed();
    }
}

