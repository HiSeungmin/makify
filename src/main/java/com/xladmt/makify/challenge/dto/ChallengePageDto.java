package com.xladmt.makify.challenge.dto;

import com.xladmt.makify.common.entity.Challenge;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChallengePageDto {
    private List<Challenge> Challenges;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private boolean hasNext;
    private boolean hasPrevious;
}
