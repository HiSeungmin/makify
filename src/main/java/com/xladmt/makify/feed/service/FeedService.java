package com.xladmt.makify.feed.service;

import com.xladmt.makify.feed.dto.FeedCommentResponse;
import com.xladmt.makify.feed.dto.FeedResponse;
import com.xladmt.makify.feed.dto.ToggleLikeResponse;

import java.util.List;

public interface FeedService {
    List<FeedResponse> getFeed(Long memberId, String sort);
    ToggleLikeResponse toggleLike(Long recordId, Long memberId);
    FeedCommentResponse addComment(Long recordId, Long memberId, String content);
    List<FeedCommentResponse> getComments(Long recordId);
    void deleteComment(Long commentId, Long memberId);
}
