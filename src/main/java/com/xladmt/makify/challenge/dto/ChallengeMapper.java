package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.entity.Challenge;
import org.springframework.stereotype.Component;

@Component
public class ChallengeMapper {
    
    public ChallengeSearchDto toSearchDto(Challenge challenge) {
        return ChallengeSearchDto.builder()
            .id(challenge.getId())
            .title(challenge.getTitle())
            .description(challenge.getDescription())
            .category(challenge.getCategory())
            .startDate(challenge.getStartDate())
            .endDate(challenge.getEndDate())
            .maxParticipants(challenge.getMaxParticipants())
            .currentParticipants(challenge.getParticipantCount())
            .creatorName(challenge.getMember().getName())
            .status(challenge.getStatus())
            .createdAt(challenge.getCreatedAt())
            .build();
    }
    
    public ChallengeSearchDto toSearchDtoWithParticipantCount(Challenge challenge, Integer participantCount) {
        return ChallengeSearchDto.builder()
            .id(challenge.getId())
            .title(challenge.getTitle())
            .description(challenge.getDescription())
            .category(challenge.getCategory())
            .startDate(challenge.getStartDate())
            .endDate(challenge.getEndDate())
            .maxParticipants(challenge.getMaxParticipants())
            .currentParticipants(participantCount)
            .creatorName(challenge.getMember().getName())
            .status(challenge.getStatus())
            .createdAt(challenge.getCreatedAt())
            .build();
    }
}
