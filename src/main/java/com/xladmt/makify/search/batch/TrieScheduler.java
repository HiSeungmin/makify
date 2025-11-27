package com.xladmt.makify.search.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Trie 주간 갱신 스케줄러
 * 매주 월요일 자정(00:00:00)에 자동으로 실행
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class TrieScheduler {

    private final TrieBuilder trieBuilder;

    /**
     * 매주 월요일 자정에 Trie 구축 및 저장
     * cron: 초 분 시 일 월 요일
     * "0 0 0 ? * MON" = 매주 월요일 00:00:00
     */
    @Scheduled(cron = "0 0 0 ? * MON")
    public void buildTrieWeekly() {
        log.info("[TRIE_SCHEDULER] ========== Trie 주간 갱신 시작 ==========");
        log.info("[TRIE_SCHEDULER] 실행 시간: {}", java.time.LocalDateTime.now());

        try {
            trieBuilder.buildAndSaveTrie();
            log.info("[TRIE_SCHEDULER] ========== Trie 주간 갱신 완료 ==========");
        } catch (Exception e) {
            log.error("[TRIE_SCHEDULER] Trie 주간 갱신 실패", e);
        }
    }

    /**
     * 개발/테스트용: 수동으로 Trie 갱신 트리거
     * 스프링 부트 시작 시 한 번 실행 (초기 데이터 로드)
     */
    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void initializeTrieOnStartup() {
        log.info("[TRIE_SCHEDULER] ========== 초기 Trie 로드 시작 ==========");

        try {
            // Redis에 Trie가 없으면 구축
            // (있으면 스킵 - TrieRedisService.hasTrieData() 사용)

            trieBuilder.buildAndSaveTrie();
            log.info("[TRIE_SCHEDULER] ========== 초기 Trie 로드 완료 ==========");
        } catch (Exception e) {
            log.error("[TRIE_SCHEDULER] 초기 Trie 로드 실패", e);
        }
    }
}
