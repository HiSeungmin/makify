package com.xladmt.makify.challenge.service;


public interface ReviewService {
    void submitReview(Long challengeId, Long memberId, String content, Integer star);
    boolean hasReview(Long challengeId, Long memberId);
    void updateReview(Long reviewId, Long memberId, String content, Integer star);
}
