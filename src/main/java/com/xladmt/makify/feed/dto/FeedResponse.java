package com.xladmt.makify.feed.dto;

import com.xladmt.makify.common.entity.ChallengeRecord;
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
    private int likeCount;
    private int commentCount;
    private boolean liked;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static FeedResponse from(ChallengeRecord record, int likeCount, int commentCount, boolean liked) {
        return FeedResponse.builder()
                .recordId(record.getId())
                .nickname(record.getMember().getNickname())
                .challengeTitle(record.getChallenge().getTitle())
                .category(record.getChallenge().getCategory().getDescription()) // 한글로 변경
                .imageUrl(record.getImageUrl())
                .memo(record.getMemo())
                .verificatedDate(record.getVerificatedDate().format(FORMATTER))
                .likeCount(likeCount)
                .commentCount(commentCount)
                .liked(liked)
                .build();
    }
}
