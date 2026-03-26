package com.xladmt.makify.feed.service;

import com.xladmt.makify.feed.dto.FeedCommentResponse;
import com.xladmt.makify.feed.dto.FeedResponse;

import java.util.List;
import java.util.Map;

public interface FeedService {
    List<FeedResponse> getFeed(Long memberId, String sort);
    Map<String, Object> toggleLike(Long recordId, Long memberId);
    FeedCommentResponse addComment(Long recordId, Long memberId, String content);
    List<FeedCommentResponse> getComments(Long recordId);
    void deleteComment(Long commentId, Long memberId);
}
