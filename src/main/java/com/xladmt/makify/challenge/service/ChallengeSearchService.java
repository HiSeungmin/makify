package com.xladmt.makify.challenge.service;

import com.xladmt.makify.challenge.dto.ChallengeSearchDto;
import com.xladmt.makify.challenge.dto.ChallengeSearchRequest;
import com.xladmt.makify.challenge.dto.ChallengeSearchResponse;
import com.xladmt.makify.common.constant.Category;
import org.springframework.data.domain.Page;

public interface ChallengeSearchService {

    /**
     * 챌린지 검색 (통합 버전)
     */
    ChallengeSearchResponse searchChallenges(ChallengeSearchRequest request);

    /**
     * 챌린지 검색 (개별 파라미터 버전) - 하위 호환성
     */
    Page<ChallengeSearchDto> searchChallenges(
        String keyword,
        Category category,
        String sortBy,
        int page,
        int size
    );
}
