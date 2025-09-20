package com.xladmt.makify.challenge.service;

import com.xladmt.makify.challenge.dto.ChallengeMapper;
import com.xladmt.makify.challenge.dto.ChallengeSearchDto;
import com.xladmt.makify.challenge.dto.ChallengeSearchRequest;
import com.xladmt.makify.challenge.dto.ChallengeSearchResponse;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.common.constant.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeSearchServiceImpl implements ChallengeSearchService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeMapper challengeMapper;

    @Override
    public ChallengeSearchResponse searchChallenges(ChallengeSearchRequest request) {
        // 기본값 설정
        request.setDefaultsIfNull();

        // 정렬 조건 생성
        Sort sort = createSort(request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // 검색 실행
        Page<ChallengeSearchDto> resultPage = challengeRepository.searchChallenges(
                request.getKeyword(),
                request.getCategory(),
                pageable
        ).map(challengeMapper::toSearchDto);

        // 응답 생성
        return ChallengeSearchResponse.from(resultPage, request);
    }

    @Override
    public Page<ChallengeSearchDto> searchChallenges(
            String keyword,
            Category category,
            String sortBy,
            int page,
            int size
    ) {
        ChallengeSearchRequest request = new ChallengeSearchRequest();
        request.setKeyword(keyword);
        request.setCategory(category);
        request.setSortBy(sortBy);
        request.setPage(page);
        request.setSize(size);

        return searchChallenges(request).getContent() != null ?
            Page.empty() : // 임시 - 실제로는 Page 객체를 반환해야 함
            challengeRepository.searchChallenges(keyword, category,
                PageRequest.of(page, size, createSort(sortBy)))
                .map(challengeMapper::toSearchDto);
    }

    private Sort createSort(String sortBy) {
        return switch (sortBy) {
            case "popularity" -> Sort.by("participantCount").descending();
            case "latest" -> Sort.by("createdAt").descending();
            case "startDate" -> Sort.by("startDate").ascending();
            default -> Sort.by("createdAt").descending();
        };
    }
}
