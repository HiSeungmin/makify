package com.xladmt.makify.challenge.controller;

import com.xladmt.makify.challenge.dto.ChallengeCreateRequest;
import com.xladmt.makify.challenge.dto.ChallengeDetailResponse;
import com.xladmt.makify.challenge.dto.ChallengeSearchRequest;
import com.xladmt.makify.challenge.dto.ChallengeSearchResponse;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.challenge.service.ChallengeSearchService;
import com.xladmt.makify.challenge.service.ChallengeServiceImpl;
import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.common.validator.ChallengeCreateRequestValidator;
import com.xladmt.makify.common.validator.ChallengeValidator;
import com.xladmt.makify.payment.dto.RequestPayDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.net.Authenticator;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChallengeController {

    private final ChallengeServiceImpl challengeService;
    private final ChallengeRepository challengeRepository;
    private final ChallengeValidator challengeValidator;
    private final ChallengeSearchService challengeSearchService;
    private final ChallengeCreateRequestValidator challengeCreateRequestValidator;

    @GetMapping("/challenges")
    public String getChallenges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model
    ) {
        // 페이징 처리된 데이터 가져오기
        List<Challenge> allChallenges = challengeService.getAllVisibleChallenges();
        
        // 수동 페이징 처리
        int totalElements = allChallenges.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<Challenge> pagedChallenges = start < totalElements ? 
            allChallenges.subList(start, end) : new java.util.ArrayList<>();
        
        model.addAttribute("challenges", pagedChallenges);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("hasNext", page < totalPages - 1);
        model.addAttribute("hasPrevious", page > 0);
        
        return "challenge/challenges";
    }

    // 챌린지 상세 패이지
    @GetMapping("/challenges/{id}")
    public String getChallengeDetail(@PathVariable Long id, Model model, @AuthenticationPrincipal MemberDetails memberDetails) {
        String loginId = memberDetails.getMember().getLoginId();
        ChallengeDetailResponse challenge = challengeService.getChallenge(loginId, id);

        model.addAttribute("loginMemberId", loginId);
        model.addAttribute("challenge", challenge);

        return "challenge/detail"; // detail.html
    }

    @GetMapping("/challenges/new")
    public String createChallengeForm() {
        return "challenge/create";
    }

    @PostMapping("/challenges/new")
    public String createChallenge(@ModelAttribute ChallengeCreateRequest request, @AuthenticationPrincipal MemberDetails memberDetails, BindingResult bindingResult) {

        log.info("챌린지 생성 중.. \n"+request);
        challengeCreateRequestValidator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            ErrorCode errorCode = challengeCreateRequestValidator.resolveErrorCode(bindingResult);
            throw new BusinessException(errorCode);
        }

        challengeService.create(request, memberDetails.getId());

        return "redirect:/challenges";
    }

    // 챌린지 참여 페이지 - 결제 정보만 표시 (DB 생성 X)
    @GetMapping("/challenges/{id}/join")
    public String showJoinPage(@PathVariable Long id, @AuthenticationPrincipal MemberDetails memberDetails, Model model) {
        // 1. 챌린지 조회
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        // 2. 참여 가능 여부 확인
        challengeValidator.validateJoinable(id, memberDetails.getId());
        
        // 3. 결제 정보만 준비 (uuid 없음)
        RequestPayDto paymentInfo = challengeService.getPaymentInfo(id, memberDetails.getId());
        
        // 4. 모델에 담기
        model.addAttribute("challenge", challenge);
        model.addAttribute("paymentInfo", paymentInfo);
        model.addAttribute("challengeId", id);
        model.addAttribute("userId", memberDetails.getId());
        
        // 5. 참여 페이지 반환
        return "challenge/join";
    }

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

            // 안전하게 숫자로 변환
            int pageNum = 0;
            int sizeNum = 20;

            try {
                pageNum = Integer.parseInt(page);
            } catch (NumberFormatException e) {
                pageNum = 0;
            }

            try {
                sizeNum = Integer.parseInt(size);
            } catch (NumberFormatException e) {
                sizeNum = 20;
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
                .collect(java.util.stream.Collectors.toList());

            // 페이징 처리
            int start = pageNum * sizeNum;
            int end = Math.min(start + sizeNum, filteredChallenges.size());
            List<Challenge> pagedChallenges = start < filteredChallenges.size() ?
                filteredChallenges.subList(start, end) : new java.util.ArrayList<>();

            // 페이지네이션 정보 (filteredChallenges.size()로 확실히 계산)
            int totalElements = filteredChallenges.size();
            int totalPages = totalElements > 0 ? (int) Math.ceil((double) totalElements / sizeNum) : 0;

            log.info("필터링 결과: 전체 {}개, 현재 페이지 {}개", totalElements, pagedChallenges.size());

            // 응답 생성
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("content", pagedChallenges.stream().map(challenge -> {
                java.util.Map<String, Object> challengeMap = new java.util.HashMap<>();
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
                return challengeMap;
            }).collect(java.util.stream.Collectors.toList()));

            response.put("totalElements", totalElements);
            response.put("totalPages", totalPages);
            response.put("currentPage", pageNum);
            response.put("hasNext", pageNum < totalPages - 1);
            response.put("hasPrevious", pageNum > 0);
            response.put("success", true);

            log.info("검색 결과: {}개 챌린지 반환 (전체: {})", pagedChallenges.size(), totalElements);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("검색 API 오류:", e);
            java.util.Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "검색 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("content", new java.util.ArrayList<>());
            errorResponse.put("totalElements", 0);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // 상태 표시명 헬퍼 메서드
    private String getStatusDisplayName(String status) {
        switch (status) {
            case "NOT_STARTED": return "모집중";
            case "IN_PROGRESS": return "진행중";
            case "COMPLETED": return "완료";
            default: return "알 수 없음";
        }
    }

}
