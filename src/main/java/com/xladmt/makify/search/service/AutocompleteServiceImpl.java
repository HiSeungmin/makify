package com.xladmt.makify.search.service;

import com.xladmt.makify.search.trie.Trie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AutocompleteServiceImpl implements AutocompleteService {

    private final TrieRedisService trieRedisService;

    // 주어진 prefix로 자동완성 검색
    @Override
    public List<String> autocomplete(String prefix) {
        try {
            // 입력값 검증
            if (prefix == null || prefix.trim().isEmpty()) {
                log.debug("[AUTOCOMPLETE] 빈 prefix로 요청됨");
                return List.of();
            }

            String trimmedPrefix = prefix.trim().toLowerCase();

            // Redis에서 최신 Trie 로드
            Trie trie = trieRedisService.getTrie();

            // Trie에서 자동완성 검색
            List<String> results = trie.autocomplete(trimmedPrefix);

            log.debug("[AUTOCOMPLETE] prefix={}, results={}", trimmedPrefix, results.size());
            return results;

        } catch (Exception e) {
            log.error("[AUTOCOMPLETE] 자동완성 검색 중 오류. prefix={}", prefix, e);
            return List.of(); // 오류 시 빈 리스트 반환
        }
    }

    // 특정 검색어가 존재하는지 확인
    @Override
    public boolean searchQueryExists(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return false;
            }

            // Redis에서 모든 검색어 조회
            Map<String, Integer> searchQueries = trieRedisService.getSearchQueries();

            boolean exists = searchQueries.containsKey(query.trim());
            log.debug("[SEARCH_EXISTS] query={}, exists={}", query, exists);
            return exists;

        } catch (Exception e) {
            log.error("[SEARCH_EXISTS] 검색어 존재 확인 중 오류. query={}", query, e);
            return false;
        }
    }

    // 현재 저장된 Trie의 단어 개수 조회
    @Override
    public int getTrieWordCount() {
        try {
            Trie trie = trieRedisService.getTrie();
            int wordCount = trie.size();
            log.debug("[TRIE_COUNT] 현재 Trie 단어 개수: {}", wordCount);
            return wordCount;

        } catch (Exception e) {
            log.error("[TRIE_COUNT] Trie 단어 개수 조회 중 오류", e);
            return 0;
        }
    }
}
