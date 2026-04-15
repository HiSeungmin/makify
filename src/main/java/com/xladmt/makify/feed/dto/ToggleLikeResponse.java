package com.xladmt.makify.feed.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ToggleLikeResponse {

    @JsonProperty("liked")
    private final boolean liked;
    private final int likeCount;

    public static ToggleLikeResponse of(boolean liked, int likeCount) {
        return new ToggleLikeResponse(liked, likeCount);
    }
}
