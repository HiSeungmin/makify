package com.xladmt.makify.search.service;

import java.util.List;

// 자동완성 검색을 제공하는 서비스
public interface AutocompleteService {

    /**
     * 주어진 prefix로 자동완성 검색
     *
     * @param prefix 검색 prefix (예: "pyt")
     * @return 상위 5개 검색어 리스트
     *        예: ["python tutorial", "python", "python course", ...]
     */
    List<String> autocomplete(String prefix);

    // 특정 검색어가 존재하는지 확인
    boolean searchQueryExists(String query);

    // 현재 저장된 Trie의 단어 개수 조회
    int getTrieWordCount();
}
