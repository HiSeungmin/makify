package com.xladmt.makify.search.service;

import com.xladmt.makify.search.trie.Trie;

import java.util.Map;

// Trie와 Redis 사이의 상호작용을 관리하는 서비스
public interface TrieRedisService {


    void saveTrie(Trie trie);
    Trie getTrie();

    /**
     * 검색어와 빈도수를 Redis Sorted Set에 저장
     * 스케줄러에서 호출하여 주간 데이터 저장
     *
     * @param queries 검색어-빈도수 맵
     *               예: {"python": 150, "python tutorial": 320, ...}
     */
    void saveSearchQueries(Map<String, Integer> queries);


    Map<String, Integer> getSearchQueries();
    boolean hasTrieData();
    void deleteTrieData();
}
