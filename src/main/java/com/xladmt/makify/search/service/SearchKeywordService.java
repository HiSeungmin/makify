package com.xladmt.makify.search.service;

import java.util.Map;
import java.util.Set;

/**
 * 검색어 통계를 관리하는 서비스 인터페이스
 */
public interface SearchKeywordService {

    /**
     * 검색어 카운팅 (실시간)
     * @param keyword 검색어
     */
    void countKeyword(String keyword);

    /**
     * 특정 날짜의 검색어별 빈도수 조회
     * @param date 날짜 (yyyy-MM-dd 형식)
     * @return 검색어별 빈도수 맵
     */
    Map<String, Integer> getKeywordCountByDate(String date);

    /**
     * 오늘의 모든 검색어 빈도수 조회
     * @return 검색어별 빈도수 맵
     */
    Map<String, Integer> getTodayKeywordCount();

    /**
     * 오늘의 검색어 통계 조회
     * @return 전체 검색 수, 고유 검색어 수 등
     */
    Map<String, Object> getTodaySearchStats();

    /**
     * Redis에서 특정 날짜의 검색어 데이터 삭제 (날짜 변경 시)
     * @param date 날짜 (yyyy-MM-dd 형식)
     */
    void deleteKeywordsByDate(String date);
}
