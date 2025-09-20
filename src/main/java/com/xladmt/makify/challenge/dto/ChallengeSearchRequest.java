package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.constant.Category;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Getter
@Setter
public class ChallengeSearchRequest {
    
    private String keyword;           // 검색 키워드 (제목, 설명)
    private Category category;        // 카테고리 필터
    private String sortBy = "latest"; // 정렬 기준: latest, popularity, startDate
    
    @Min(0)
    private int page = 0;            // 페이지 번호 (0부터 시작)
    
    @Min(1)
    @Max(100)
    private int size = 20;           // 페이지 크기
    
    // 정렬 기준 검증
    public boolean isValidSortBy() {
        return "latest".equals(sortBy) || 
               "popularity".equals(sortBy) || 
               "startDate".equals(sortBy);
    }
    
    // 기본값 설정
    public void setDefaultsIfNull() {
        if (sortBy == null || !isValidSortBy()) {
            sortBy = "latest";
        }
        if (page < 0) {
            page = 0;
        }
        if (size < 1 || size > 100) {
            size = 20;
        }
    }
    
    // 검색 조건이 있는지 확인
    public boolean hasSearchConditions() {
        return (keyword != null && !keyword.trim().isEmpty()) || 
               category != null;
    }
}
