package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.constant.Category;
import com.xladmt.makify.common.constant.ChallengeStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ChallengeSearchDto {
    
    private Long id;
    private String title;
    private String description;
    private Category category;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String creatorName;
    private ChallengeStatus status;
    private LocalDateTime createdAt;
    
    // 추가 정보
    private Integer durationDays;      // 챌린지 기간 (일)
    private Integer daysUntilStart;    // 시작까지 남은 일수
    private Double participationRate;   // 참여율 (currentParticipants / maxParticipants * 100)
    private String thumbnailUrl;       // 썸네일 이미지 (추후 추가)
    
    // 계산된 필드들을 위한 빌더 후처리
//    public static class ChallengeSearchDtoBuilder {
//        public ChallengeSearchDto build() {
//            ChallengeSearchDto dto = new ChallengeSearchDto(
//                id, title, description, category, startDate, endDate,
//                maxParticipants, currentParticipants, creatorName, status, createdAt,
//                durationDays, daysUntilStart, participationRate, thumbnailUrl
//            );
//
//            // 계산된 필드들 설정
//            if (dto.startDate != null && dto.endDate != null) {
//                dto = dto.toBuilder()
//                    .durationDays((int) java.time.temporal.ChronoUnit.DAYS.between(dto.startDate, dto.endDate) + 1)
//                    .build();
//            }
//
//            if (dto.startDate != null) {
//                dto = dto.toBuilder()
//                    .daysUntilStart((int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dto.startDate))
//                    .build();
//            }
//
//            if (dto.maxParticipants != null && dto.currentParticipants != null && dto.maxParticipants > 0) {
//                dto = dto.toBuilder()
//                    .participationRate((double) dto.currentParticipants / dto.maxParticipants * 100)
//                    .build();
//            }
//
//            return dto;
//        }
//    }
    
    // 편의 메서드들
    public boolean isRecruiting() {
        return status == ChallengeStatus.NOT_STARTED && currentParticipants < maxParticipants;
    }
    
    public boolean isFull() {
        return currentParticipants >= maxParticipants;
    }
    
    public boolean isStartingSoon() {
        return daysUntilStart != null && daysUntilStart >= 0 && daysUntilStart <= 7;
    }
    
    public String getStatusDisplayName() {
        return switch (status) {
            case NOT_STARTED -> "모집중";
            case IN_PROGRESS -> "진행중";
            case COMPLETED -> "완료";
            default -> "알 수 없음";
        };
    }
    
    public String getCategoryDisplayName() {
        return category != null ? category.getDescription() : "";
    }
}
