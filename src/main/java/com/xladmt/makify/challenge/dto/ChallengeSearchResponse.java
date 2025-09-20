package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.constant.Category;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ChallengeSearchResponse {
    
    // 검색 결과
    private List<ChallengeSearchDto> content;
    
    // 페이징 정보
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
    
    // 검색 조건
    private String keyword;
    private Category category;
    private String sortBy;
    
    // 필터 옵션 (프론트엔드용)
    private Map<String, Object> filterOptions;
    
    public static ChallengeSearchResponse from(
            Page<ChallengeSearchDto> page, 
            ChallengeSearchRequest request
    ) {
        return ChallengeSearchResponse.builder()
            .content(page.getContent())
            .totalPages(page.getTotalPages())
            .totalElements(page.getTotalElements())
            .currentPage(page.getNumber())
            .size(page.getSize())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .keyword(request.getKeyword())
            .category(request.getCategory())
            .sortBy(request.getSortBy())
            .filterOptions(createFilterOptions())
            .build();
    }
    
    private static Map<String, Object> createFilterOptions() {
        return Map.of(
            "categories", List.of(
                Map.of("value", "ROUTINE", "label", "규칙적인 생활"),
                Map.of("value", "EXERCISE", "label", "운동"),
                Map.of("value", "MINDSET", "label", "마음챙김"),
                Map.of("value", "EATING_HABITS", "label", "식습관"),
                Map.of("value", "HOBBY", "label", "취미"),
                Map.of("value", "STUDY", "label", "공부")
            ),
            "sortOptions", List.of(
                Map.of("value", "latest", "label", "최신순"),
                Map.of("value", "popularity", "label", "인기순"),
                Map.of("value", "startDate", "label", "곧 시작순")
            )
        );
    }
}
