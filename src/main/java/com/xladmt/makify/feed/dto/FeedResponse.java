package com.xladmt.makify.feed.dto;

import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.challenge.domain.ChallengeRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class FeedResponse {

    private Long recordId;
    private String nickname;
    private String challengeTitle;
    private String category;
    private String imageUrl;
    private String memo;
    private String verificatedDate;
    private String verificatedDateIso;
    private int likeCount;
    private int commentCount;
    private boolean liked;
    private boolean isPublic;
    private boolean isOwner;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static FeedResponse from(ChallengeRecord record, int likeCount, int commentCount, boolean liked, Long currentMemberId) {
        return FeedResponse.builder()
                .recordId(record.getId())
                .nickname(record.getMember().getNickname())
                .challengeTitle(record.getChallenge().getTitle())
                .category(record.getChallenge().getCategory().getDescription())
                .imageUrl(record.getImageUrl())
                .memo(record.getMemo())
                .verificatedDate(record.getVerificatedDate().format(FORMATTER))
                .verificatedDateIso(record.getVerificatedDate().format(ISO_FORMATTER))
                .likeCount(likeCount)
                .commentCount(commentCount)
                .liked(liked)
                .isPublic(YN.Y.equals(record.getIsPublic()))
                .isOwner(currentMemberId != null && currentMemberId.equals(record.getMember().getId()))
                .build();
    }
}
