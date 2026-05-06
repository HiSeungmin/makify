package com.xladmt.makify.challenge.controller;

import com.xladmt.makify.challenge.service.ChallengeServiceImpl;
import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.challenge.domain.Challenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.validator.ChallengeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChallengeApiController {
    private static int PAGE_NUM;
    private static int SIZE_NUM;

    private final ChallengeServiceImpl challengeService;
    private final ChallengeValidator challengeValidator;

    @GetMapping("/api/challenges/search")
    @ResponseBody
    public ResponseEntity<?> searchChallenges(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,  // 상태 필터 추가
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "0") String page,
            @RequestParam(defaultValue = "20") String size
    ) {
        try {
            log.info("검색 API 호출: keyword={}, category={}, status={}, sortBy={}",
                    keyword, category, status, sortBy);

            try {
                PAGE_NUM = Integer.parseInt(page);
            } catch (NumberFormatException e) {
                PAGE_NUM = 0;
            }

            try {
                SIZE_NUM = Integer.parseInt(size);
            } catch (NumberFormatException e) {
                SIZE_NUM = 20;
            }

            // 기본 챌린지 목록 가져오기
            List<Challenge> allChallenges = challengeService.getAllVisibleChallenges();
            log.info("전체 챌린지 개수: {}", allChallenges.size());

            // 상태별 개수 확인
            if (status != null && !status.trim().isEmpty()) {
                long statusCount = allChallenges.stream()
                        .filter(c -> c.getStatus().name().equals(status))
                        .count();
                log.info("상태 '{}' 필터링 전 예상 개수: {}", status, statusCount);
            }

            // 필터링 및 정렬
            List<Challenge> filteredChallenges = allChallenges.stream()
                    .filter(challenge -> {
                        // 키워드 필터링
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            return challenge.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                                    challenge.getDescription().toLowerCase().contains(keyword.toLowerCase());
                        }
                        return true;
                    })
                    .filter(challenge -> {
                        // 카테고리 필터링
                        if (category != null && !category.trim().isEmpty()) {
                            return challenge.getCategory().name().equals(category);
                        }
                        return true;
                    })
                    .filter(challenge -> {
                        // 상태 필터링
                        if (status != null && !status.trim().isEmpty()) {
                            boolean statusMatch = challenge.getStatus().name().equals(status);
                            log.debug("상태 필터링: {} (요청: {}) = {}", challenge.getStatus().name(), status, statusMatch);
                            return statusMatch;
                        }
                        return true;
                    })
                    .filter(challenge -> {
                        // 곧 시작 순인 경우 모집중(NOT_STARTED)인 것만
                        if ("startDate".equals(sortBy)) {
                            return challenge.getStatus().name().equals("NOT_STARTED");
                        }
                        return true;
                    })
                    .sorted((c1, c2) -> {
                        // 정렬
                        switch (sortBy) {
                            case "popularity":
                                Integer count1 = c1.getParticipantCount() != null ? c1.getParticipantCount() : 0;
                                Integer count2 = c2.getParticipantCount() != null ? c2.getParticipantCount() : 0;
                                return count2.compareTo(count1); // 인기순 (참여자 많은 순)
                            case "startDate":
                                return c1.getStartDate().compareTo(c2.getStartDate()); // 시작일 빠른 순
                            case "latest":
                            default:
                                return c2.getCreatedAt().compareTo(c1.getCreatedAt()); // 최신순
                        }
                    })
                    .collect(Collectors.toList());

            // 페이징 처리
            int start = PAGE_NUM * SIZE_NUM;
            int end = Math.min(start + SIZE_NUM, filteredChallenges.size());
            List<Challenge> pagedChallenges = start < filteredChallenges.size() ?
                    filteredChallenges.subList(start, end) : new ArrayList<>();

            // 페이지네이션 정보 (filteredChallenges.size()로 확실히 계산)
            int totalElements = filteredChallenges.size();
            int totalPages = totalElements > 0 ? (int) Math.ceil((double) totalElements / SIZE_NUM) : 0;

            log.info("필터링 결과: 전체 {}개, 현재 페이지 {}개", totalElements, pagedChallenges.size());

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("content", pagedChallenges.stream().map(challenge -> {
                Map<String, Object> challengeMap = new HashMap<>();
                challengeMap.put("id", challenge.getId());
                challengeMap.put("title", challenge.getTitle());
                challengeMap.put("description", challenge.getDescription());
                challengeMap.put("category", challenge.getCategory().name());
                challengeMap.put("categoryDisplayName", challenge.getCategory().getDescription());
                challengeMap.put("startDate", challenge.getStartDate().toString());
                challengeMap.put("endDate", challenge.getEndDate().toString());
                challengeMap.put("maxParticipants", challenge.getMaxParticipants());
                challengeMap.put("currentParticipants", challenge.getParticipantCount() != null ? challenge.getParticipantCount() : 0);
                challengeMap.put("status", challenge.getStatus().name());
                challengeMap.put("statusDisplayName", getStatusDisplayName(challenge.getStatus().name()));
                challengeMap.put("progressPercentage", challenge.getProgressPercentage());
                challengeMap.put("thumbnailUrl", challenge.getThumbnailUrl());
                return challengeMap;
            }).collect(Collectors.toList()));

            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", PAGE_NUM);
            response.put("hasNext", PAGE_NUM < totalPages - 1);
            response.put("hasPrevious", PAGE_NUM > 0);
            response.put("success", true);

            log.info("검색 결과: {}개 챌린지 반환 (전체: {})", pagedChallenges.size(), totalElements);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("검색 API 오류:", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "검색 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("content", new ArrayList<>());
            errorResponse.put("totalElements", 0);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // 상태 표시명 메서드
    private String getStatusDisplayName(String status) {
        switch (status) {
            case "NOT_STARTED": return "모집중";
            case "IN_PROGRESS": return "진행중";
            case "COMPLETED": return "완료";
            default: return "알 수 없음";
        }
    }

    // 비공개 챌린지 참여 코드 검증 API
    @PostMapping("/api/challenges/{id}/verify-code")
    @ResponseBody
    public ResponseEntity<?> verifyPrivateCode(
            @PathVariable("id") Long challengeId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal MemberDetails memberDetails
    ) {
        try {
            String code = body.get("code");
            challengeValidator.validatePrivateCode(challengeId, code);
            return ResponseEntity.ok(Map.of("valid", true));
        } catch (BusinessException e) {
            return ResponseEntity.ok(Map.of("valid", false, "message", e.getErrorCode().getMessage()));
        }
    }

}
