package com.xladmt.makify.challenge.service;

import com.xladmt.makify.challenge.repository.ReviewRepository;
import com.xladmt.makify.challenge.repository.ChallengeRepository;
import com.xladmt.makify.common.entity.Challenge;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.entity.Review;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ChallengeRepository challengeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void submitReview(Long challengeId, Long memberId, String content, Integer star) {
        if (reviewRepository.existsByMemberIdAndChallengeId(memberId, challengeId)) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        reviewRepository.save(Review.create(challenge, member, content, star));
    }

    public boolean hasReview(Long challengeId, Long memberId) {
        return reviewRepository.existsByMemberIdAndChallengeId(memberId, challengeId);
    }

    @Transactional
    public void updateReview(Long reviewId, Long memberId, String content, Integer star) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        if (!review.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        review.update(content, star);
    }
}
