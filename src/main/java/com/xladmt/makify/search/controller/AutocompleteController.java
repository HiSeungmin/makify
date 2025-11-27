package com.xladmt.makify.search.controller;

import com.xladmt.makify.search.service.AutocompleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class AutocompleteController {

    private final AutocompleteService autocompleteService;

    /**
     * 자동완성 검색
     *
     * @param prefix 검색 prefix (예: "pyt")
     * @return 상위 5개 검색어 리스트
     *
     * 예: GET /api/search/autocomplete?prefix=챌
     * 응답: {
     *   "success": true,
     *   "results": ["챌린지", "챌린지 시작하기", ...],
     *   "count": 5
     * }
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<?> autocomplete(@RequestParam(required = false) String prefix) {
        try {
            log.debug("[AUTOCOMPLETE] 자동완성 요청. prefix={}", prefix);

            // 입력값 검증
            if (prefix == null || prefix.trim().isEmpty()) {
                log.debug("[AUTOCOMPLETE] 빈 prefix");
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("results", List.of());
                response.put("count", 0);
                return ResponseEntity.ok(response);
            }

            String trimmedPrefix = prefix.trim();

            // 자동완성 검색
            List<String> results = autocompleteService.autocomplete(trimmedPrefix);

            log.debug("[AUTOCOMPLETE] 결과: {}개", results.size());

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("results", results);
            response.put("count", results.size());
            response.put("prefix", trimmedPrefix);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[AUTOCOMPLETE] 자동완성 검색 중 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "자동완성 검색 중 오류가 발생했습니다");
            errorResponse.put("results", List.of());
            errorResponse.put("count", 0);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 검색어 존재 여부 확인
     *
     * @param query 검색어 (예: "챌린지")
     * @return 존재 여부
     *
     * 예: GET /api/search/exists?query=챌린지
     * 응답: {
     *   "success": true,
     *   "exists": true,
     *   "query": "챌린지"
     * }
     */
    @GetMapping("/exists")
    public ResponseEntity<?> searchQueryExists(@RequestParam(required = false) String query) {
        try {
            log.debug("[SEARCH_EXISTS] 검색어 존재 확인. query={}", query);

            // 입력값 검증
            if (query == null || query.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("exists", false);
                response.put("query", "");
                return ResponseEntity.ok(response);
            }

            String trimmedQuery = query.trim();

            // 검색어 존재 확인
            boolean exists = autocompleteService.searchQueryExists(trimmedQuery);

            log.debug("[SEARCH_EXISTS] query={}, exists={}", trimmedQuery, exists);

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("exists", exists);
            response.put("query", trimmedQuery);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[SEARCH_EXISTS] 검색어 존재 확인 중 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "검색어 확인 중 오류가 발생했습니다");
            errorResponse.put("exists", false);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Trie 통계 조회
     *
     * @return Trie 정보
     *
     * 예: GET /api/search/trie-info
     * 응답: {
     *   "success": true,
     *   "wordCount": 12345,
     *   "message": "현재 12345개의 검색어가 저장되어 있습니다"
     * }
     */
    @GetMapping("/trie-info")
    public ResponseEntity<?> getTrieInfo() {
        try {
            log.debug("[TRIE_INFO] Trie 통계 조회");

            int wordCount = autocompleteService.getTrieWordCount();

            log.debug("[TRIE_INFO] 단어 개수: {}", wordCount);

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("wordCount", wordCount);
            response.put("message", String.format("현재 %d개의 검색어가 저장되어 있습니다", wordCount));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[TRIE_INFO] Trie 통계 조회 중 오류", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Trie 통계 조회 중 오류가 발생했습니다");
            errorResponse.put("wordCount", 0);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 헬스 체크
     *
     * @return 서버 상태
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("status", "OK");
        response.put("message", "자동완성 검색 서비스가 정상 작동 중입니다");
        return ResponseEntity.ok(response);
    }
}
