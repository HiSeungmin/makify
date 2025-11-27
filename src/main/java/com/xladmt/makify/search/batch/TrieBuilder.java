package com.xladmt.makify.search.batch;

import com.xladmt.makify.search.service.TrieRedisService;
import com.xladmt.makify.search.trie.Trie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Redis의 검색어 데이터를 읽어서
 * 새로운 Trie를 구축하고 저장하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrieBuilder {

    private final TrieRedisService trieRedisService;
    private final SearchDataReader searchDataReader;

    /**
     * Trie 구축 및 Redis 저장
     * 1. 검색 데이터 수집 (로그에서 읽기)
     * 2. 새로운 Trie 구축
     * 3. Redis에 저장
     */
    public void buildAndSaveTrie() {
        try {
            log.info("[TRIE_BUILDER] Trie 구축 시작");

            long startTime = System.currentTimeMillis();

            // Step 1: 검색 데이터 수집 (로그에서 읽기)
            log.info("[TRIE_BUILDER] Step 1: 검색 데이터 수집 중...");
            Map<String, Integer> searchQueries = searchDataReader.readSearchDataForLastWeek();

            if (searchQueries.isEmpty()) {
                log.warn("[TRIE_BUILDER] 검색 데이터가 없습니다. 빈 Trie로 저장합니다.");
            } else {
                log.info("[TRIE_BUILDER] 수집된 검색어: {}개", searchQueries.size());
            }

            // Step 2: 새로운 Trie 구축
            log.info("[TRIE_BUILDER] Step 2: Trie 구축 중...");
            Trie newTrie = new Trie();
            newTrie.rebuild(searchQueries);

            log.info("[TRIE_BUILDER] Trie 구축 완료. 단어 개수: {}", newTrie.size());

            // Step 3: Redis에 저장
            log.info("[TRIE_BUILDER] Step 3: Redis에 저장 중...");
            trieRedisService.saveTrie(newTrie);

            // 검색 데이터도 Redis에 저장 (다음 주 갱신을 위해)
            trieRedisService.saveSearchQueries(searchQueries);

            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            log.info("[TRIE_BUILDER] Trie 구축 완료! 소요 시간: {}ms", elapsedTime);

        } catch (Exception e) {
            log.error("[TRIE_BUILDER] Trie 구축 중 오류 발생", e);
            throw new RuntimeException("Trie 구축 실패", e);
        }
    }

    /**
     * 주어진 검색 데이터로 Trie 구축 및 저장 (테스트용)
     *
     * @param searchQueries 검색어-빈도수 맵
     */
    public void buildAndSaveTrieWithData(Map<String, Integer> searchQueries) {
        try {
            log.info("[TRIE_BUILDER] Trie 구축 시작 (커스텀 데이터)");

            long startTime = System.currentTimeMillis();

            if (searchQueries == null || searchQueries.isEmpty()) {
                log.warn("[TRIE_BUILDER] 검색 데이터가 없습니다. 빈 Trie로 저장합니다.");
                searchQueries = new java.util.HashMap<>();
            }

            // Trie 구축
            Trie newTrie = new Trie();
            newTrie.rebuild(searchQueries);

            log.info("[TRIE_BUILDER] Trie 구축 완료. 단어 개수: {}", newTrie.size());

            // Redis에 저장
            trieRedisService.saveTrie(newTrie);
            trieRedisService.saveSearchQueries(searchQueries);

            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            log.info("[TRIE_BUILDER] Trie 구축 완료! 소요 시간: {}ms", elapsedTime);

        } catch (Exception e) {
            log.error("[TRIE_BUILDER] Trie 구축 중 오류 발생", e);
            throw new RuntimeException("Trie 구축 실패", e);
        }
    }
}
