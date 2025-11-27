package com.xladmt.makify.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xladmt.makify.search.trie.Trie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrieRedisServiceImpl implements TrieRedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis 키 상수
    private static final String TRIE_KEY = "trie:latest";
    private static final String SEARCH_QUERIES_KEY = "search_queries";

    // Trie를 JSON으로 직렬화하여 Redis에 저장
    @Override
    public void saveTrie(Trie trie) {
        try {
            // Trie를 JSON 문자열로 변환
            String trieJson = objectMapper.writeValueAsString(trie);

            // Redis에 저장
            redisTemplate.opsForValue().set(TRIE_KEY, trieJson);

            log.info("[TRIE] Trie 저장 완료. 단어 개수: {}", trie.size());
        } catch (Exception e) {
            log.error("[TRIE] Trie 저장 중 오류", e);
            throw new RuntimeException("Trie 저장 실패", e);
        }
    }


    @Override
    public Trie getTrie() {
        try {
            // Redis에서 JSON 문자열 로드
            Object trieData = redisTemplate.opsForValue().get(TRIE_KEY);

            if (trieData == null) {
                log.warn("[TRIE] Redis에 저장된 Trie 데이터가 없습니다");
                return new Trie(); // 빈 Trie 반환
            }

            // JSON을 Trie 객체로 변환
            String trieJson = (String) trieData;
            Trie trie = objectMapper.readValue(trieJson, Trie.class);

            log.debug("[TRIE] Trie 로드 완료. 단어 개수: {}", trie.size());
            return trie;

        } catch (Exception e) {
            log.error("[TRIE] Trie 로드 중 오류", e);
            return new Trie(); // 오류 시 빈 Trie 반환
        }
    }

    /**
     * 검색어와 빈도수를 Redis Sorted Set에 저장
     * 매주 TrieScheduler에서 호출됨
     */
    @Override
    public void saveSearchQueries(Map<String, Integer> queries) {
        try {
            if (queries == null || queries.isEmpty()) {
                log.warn("[SEARCH_QUERIES] 저장할 검색어 데이터가 없습니다");
                return;
            }

            // 기존 데이터 삭제 (새로운 주간 데이터로 교체)
            redisTemplate.delete(SEARCH_QUERIES_KEY);

            // 각 검색어와 빈도수를 Sorted Set에 저장
            for (Map.Entry<String, Integer> entry : queries.entrySet()) {
                String keyword = entry.getKey();
                double frequency = entry.getValue();

                redisTemplate.opsForZSet().add(SEARCH_QUERIES_KEY, keyword, frequency);
            }

            log.info("[SEARCH_QUERIES] 검색어 데이터 저장 완료. 개수: {}", queries.size());

        } catch (Exception e) {
            log.error("[SEARCH_QUERIES] 검색어 데이터 저장 중 오류", e);
            throw new RuntimeException("검색어 데이터 저장 실패", e);
        }
    }

    /**
     * Redis에서 모든 검색어와 빈도수 조회
     * TrieBuilder에서 호출되어 Trie 구축에 사용
     */
    @Override
    public Map<String, Integer> getSearchQueries() {
        try {
            Map<String, Integer> result = new HashMap<>();

            // Sorted Set에서 모든 멤버와 점수(빈도수) 조회
            Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> tuples =
                    redisTemplate.opsForZSet().rangeWithScores(SEARCH_QUERIES_KEY, 0, -1);

            if (tuples == null || tuples.isEmpty()) {
                log.warn("[SEARCH_QUERIES] Redis에 저장된 검색어 데이터가 없습니다");
                return result;
            }

            // TypedTuple을 Map으로 변환
            for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object> tuple : tuples) {
                String keyword = (String) tuple.getValue();
                int frequency = tuple.getScore().intValue();
                result.put(keyword, frequency);
            }

            log.debug("[SEARCH_QUERIES] 검색어 데이터 로드 완료. 개수: {}", result.size());
            return result;

        } catch (Exception e) {
            log.error("[SEARCH_QUERIES] 검색어 데이터 로드 중 오류", e);
            return new HashMap<>(); // 오류 시 빈 Map 반환
        }
    }


    @Override
    public boolean hasTrieData() {
        try {
            return redisTemplate.hasKey(TRIE_KEY);
        } catch (Exception e) {
            log.error("[TRIE] Trie 데이터 존재 확인 중 오류", e);
            return false;
        }
    }

    @Override
    public void deleteTrieData() {
        try {
            redisTemplate.delete(TRIE_KEY);
            log.info("[TRIE] Trie 데이터 삭제 완료");
        } catch (Exception e) {
            log.error("[TRIE] Trie 데이터 삭제 중 오류", e);
        }
    }
}
