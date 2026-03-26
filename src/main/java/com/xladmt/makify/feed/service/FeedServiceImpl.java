package com.xladmt.makify.feed.service;

import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.ChallengeRecord;
import com.xladmt.makify.common.entity.FeedComment;
import com.xladmt.makify.common.entity.FeedLike;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.feed.dto.FeedCommentResponse;
import com.xladmt.makify.feed.dto.FeedResponse;
import com.xladmt.makify.feed.repository.FeedCommentRepository;
import com.xladmt.makify.feed.repository.FeedLikeRepository;
import com.xladmt.makify.feed.repository.FeedRepository;
import com.xladmt.makify.member.repository.MemberRepository;
import com.xladmt.makify.verification.repository.ChallengeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final MemberRepository memberRepository;
    private final ChallengeRecordRepository challengeRecordRepository;

    @Override
    public List<FeedResponse> getFeed(Long memberId, String sort) {
        List<ChallengeRecord> records = "likes".equals(sort)
                ? feedRepository.findFeedOrderByLikes(YN.Y, YN.Y)
                : feedRepository.findFeedOrderByLatest(YN.Y, YN.Y);

        return records.stream()
                .map(record -> {
                    int likeCount = feedLikeRepository.countByRecordId(record.getId());
                    int commentCount = feedCommentRepository.countByRecordId(record.getId());
                    boolean liked = memberId != null &&
                            feedLikeRepository.findByRecordIdAndMemberId(record.getId(), memberId).isPresent();
                    return FeedResponse.from(record, likeCount, commentCount, liked);
                })
                .toList();
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long recordId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        ChallengeRecord record = challengeRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        Optional<FeedLike> existing = feedLikeRepository.findByRecordIdAndMemberId(recordId, memberId);

        if (existing.isPresent()) {
            feedLikeRepository.delete(existing.get());
            int count = feedLikeRepository.countByRecordId(recordId);
            return Map.of("liked", false, "likeCount", count);
        } else {
            feedLikeRepository.save(FeedLike.create(record, member));
            int count = feedLikeRepository.countByRecordId(recordId);
            return Map.of("liked", true, "likeCount", count);
        }
    }

    @Override
    @Transactional
    public FeedCommentResponse addComment(Long recordId, Long memberId, String content) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        ChallengeRecord record = challengeRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        FeedComment comment = FeedComment.create(record, member, content);
        feedCommentRepository.save(comment);
        return FeedCommentResponse.from(comment);
    }

    @Override
    public List<FeedCommentResponse> getComments(Long recordId) {
        return feedCommentRepository.findByRecordId(recordId, YN.Y)
                .stream()
                .map(FeedCommentResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        FeedComment comment = feedCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        comment.delete();
    }
}
