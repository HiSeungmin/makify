package com.xladmt.makify.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 검색어 통계를 Redis에서 관리하는 서비스 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchKeywordServiceImpl implements SearchKeywordService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String SEARCH_KEYWORD_PREFIX = "search:keyword:";
    private static final String SEARCH_STATS_PREFIX = "search:stats:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 검색어 카운팅
     * Redis Hash 구조: search:keyword:2025-10-30 -> {keyword: count}
     */
    @Override
    public void countKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = SEARCH_KEYWORD_PREFIX + today;
        String trimmedKeyword = keyword.trim();

        try {
            // 검색어 카운팅
            redisTemplate.opsForHash().increment(key, trimmedKeyword, 1);
            
            // 키 만료 시간 설정 (7일)
            redisTemplate.expire(key, java.time.Duration.ofDays(7));

            // 통계 업데이트
            updateSearchStats(today);

            log.debug("[SEARCH COUNT] keyword={}, date={}", trimmedKeyword, today);
        } catch (Exception e) {
            log.error("검색어 카운팅 중 오류: keyword={}", keyword, e);
        }
    }

    /**
     * 특정 날짜의 검색어별 빈도수 조회
     */
    @Override
    public Map<String, Integer> getKeywordCountByDate(String date) {
        String key = SEARCH_KEYWORD_PREFIX + date;
        Map<Object, Object> redisMap = redisTemplate.opsForHash().entries(key);

        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : redisMap.entrySet()) {
            String keyword = (String) entry.getKey();
            Integer count = Integer.parseInt((String) entry.getValue());
            result.put(keyword, count);
        }

        return result;
    }

    /**
     * 오늘의 모든 검색어 빈도수 조회
     */
    @Override
    public Map<String, Integer> getTodayKeywordCount() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return getKeywordCountByDate(today);
    }

    /**
     * 오늘의 검색어 통계 조회
     */
    @Override
    public Map<String, Object> getTodaySearchStats() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String statsKey = SEARCH_STATS_PREFIX + today;

        Map<Object, Object> stats = redisTemplate.opsForHash().entries(statsKey);
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<Object, Object> entry : stats.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            // 숫자 값은 정수로 변환
            if ("total".equals(key) || "unique_keywords".equals(key)) {
                result.put(key, Integer.parseInt(value));
            } else {
                result.put(key, value);
            }
        }

        return result;
    }

    /**
     * Redis에서 특정 날짜의 검색어 데이터 삭제
     */
    @Override
    public void deleteKeywordsByDate(String date) {
        String key = SEARCH_KEYWORD_PREFIX + date;
        String statsKey = SEARCH_STATS_PREFIX + date;

        try {
            redisTemplate.delete(key);
            redisTemplate.delete(statsKey);
            log.info("검색어 데이터 삭제: date={}", date);
        } catch (Exception e) {
            log.error("검색어 데이터 삭제 중 오류: date={}", date, e);
        }
    }

    /**
     * 검색 통계 업데이트
     */
    private void updateSearchStats(String date) {
        String key = SEARCH_KEYWORD_PREFIX + date;
        String statsKey = SEARCH_STATS_PREFIX + date;

        try {
            Map<Object, Object> keywordMap = redisTemplate.opsForHash().entries(key);
            
            // 전체 검색 수 계산
            int total = keywordMap.values().stream()
                .mapToInt(v -> Integer.parseInt((String) v))
                .sum();

            // 고유 검색어 수
            int uniqueCount = keywordMap.size();

            // 통계 저장
            redisTemplate.opsForHash().put(statsKey, "total", String.valueOf(total));
            redisTemplate.opsForHash().put(statsKey, "unique_keywords", String.valueOf(uniqueCount));
            redisTemplate.opsForHash().put(statsKey, "timestamp", String.valueOf(System.currentTimeMillis()));

            // 키 만료 시간 설정 (7일)
            redisTemplate.expire(statsKey, java.time.Duration.ofDays(7));

        } catch (Exception e) {
            log.error("검색 통계 업데이트 중 오류: date={}", date, e);
        }
    }
}
