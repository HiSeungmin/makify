package com.xladmt.makify.feed.dto;

import com.xladmt.makify.common.entity.FeedComment;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class FeedCommentResponse {

    private Long id;
    private String nickname;
    private String content;
    private String createdAt;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

    public static FeedCommentResponse from(FeedComment comment) {
        return FeedCommentResponse.builder()
                .id(comment.getId())
                .nickname(comment.getMember().getNickname())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt().format(FORMATTER))
                .build();
    }
}
