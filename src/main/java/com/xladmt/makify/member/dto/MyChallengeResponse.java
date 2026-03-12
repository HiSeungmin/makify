package com.xladmt.makify.member.dto;

import com.xladmt.makify.common.entity.UserChallenge;
import lombok.Getter;

import java.util.List;

@Getter
public class MyChallengeResponse {

    private final List<ChallengeItem> inProgress;
    private final List<ChallengeItem> notStarted;
    private final List<ChallengeItem> completed;

    public MyChallengeResponse(List<UserChallenge> inProgress,
                               List<UserChallenge> notStarted,
                               List<UserChallenge> completed,
                               java.util.function.Function<UserChallenge, Integer> todayCountProvider) {
        this.inProgress = inProgress.stream().map(uc -> new ChallengeItem(uc, todayCountProvider.apply(uc))).toList();
        this.notStarted = notStarted.stream().map(uc -> new ChallengeItem(uc, 0)).toList();
        this.completed = completed.stream().map(uc -> new ChallengeItem(uc, 0)).toList();
    }

    @Getter
    public static class ChallengeItem {
        private final Long challengeId;
        private final String title;
        private final String description;
        private final String startDate;
        private final String endDate;
        private final int currentParticipants;
        private final int maxParticipants;
        private final int progressPercentage;
        private final int targetFrequency;
        private final int todayVerifiedCount;

        public ChallengeItem(UserChallenge uc, int todayVerifiedCount) {
            this.challengeId         = uc.getChallenge().getId();
            this.title               = uc.getChallenge().getTitle();
            this.description         = uc.getChallenge().getDescription();
            this.startDate           = uc.getChallenge().getStartDate().toString();
            this.endDate             = uc.getChallenge().getEndDate().toString();
            this.currentParticipants = uc.getChallenge().getParticipantCount() == null ? 0 : uc.getChallenge().getParticipantCount();
            this.maxParticipants     = uc.getChallenge().getMaxParticipants();
            this.progressPercentage  = uc.getChallenge().getProgressPercentage();
            this.targetFrequency     = uc.getTargetFrequency() == null ? 1 : uc.getTargetFrequency();
            this.todayVerifiedCount  = todayVerifiedCount;
        }
    }
}
