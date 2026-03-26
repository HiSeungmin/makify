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
    private String verificatedDate;    // 포맷된 날짜 (yyyy.MM.dd HH:mm)
    private String verificatedDateIso; // ISO 형식 (JS 상대시간 계산용)
    private int likeCount;
    private int commentCount;
    private boolean liked;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static FeedResponse from(ChallengeRecord record, int likeCount, int commentCount, boolean liked) {
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
                .build();
    }
}
