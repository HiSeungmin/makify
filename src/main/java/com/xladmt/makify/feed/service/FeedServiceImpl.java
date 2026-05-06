package com.xladmt.makify.feed.service;

import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.constant.NotificationType;
import com.xladmt.makify.challenge.domain.ChallengeRecord;
import com.xladmt.makify.feed.domain.FeedComment;
import com.xladmt.makify.feed.domain.FeedLike;
import com.xladmt.makify.member.domain.Member;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.feed.dto.FeedCommentResponse;
import com.xladmt.makify.feed.dto.FeedResponse;
import com.xladmt.makify.feed.dto.ToggleLikeResponse;
import com.xladmt.makify.feed.repository.FeedCommentRepository;
import com.xladmt.makify.feed.repository.FeedLikeRepository;
import com.xladmt.makify.feed.repository.FeedRepository;
import com.xladmt.makify.member.repository.MemberRepository;
import com.xladmt.makify.notification.service.NotificationService;
import com.xladmt.makify.verification.repository.ChallengeRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final FeedCommentRepository feedCommentRepository;
    private final MemberRepository memberRepository;
    private final ChallengeRecordRepository challengeRecordRepository;
    private final NotificationService notificationService;

    @Override
    public List<FeedResponse> getFeed(Long memberId, String sort) {
        List<ChallengeRecord> records = "likes".equals(sort)
                ? feedRepository.findFeedOrderByLikes(YN.Y, YN.Y, YN.Y)
                : feedRepository.findFeedOrderByLatest(YN.Y, YN.Y, YN.Y);

        return records.stream()
                .map(record -> {
                    int likeCount = feedLikeRepository.countByRecordId(record.getId());
                    int commentCount = feedCommentRepository.countByRecordId(record.getId());
                    boolean liked = memberId != null &&
                            feedLikeRepository.findByRecordIdAndMemberId(record.getId(), memberId).isPresent();
                    return FeedResponse.from(record, likeCount, commentCount, liked, memberId);
                })
                .toList();
    }

    @Override
    @Transactional
    public ToggleLikeResponse toggleLike(Long recordId, Long memberId) {
        ChallengeRecord record = challengeRecordRepository.findByIdWithMember(recordId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        Optional<FeedLike> existing = feedLikeRepository.findByRecordIdAndMemberId(recordId, memberId);
        boolean isLiked;

        if (existing.isPresent()) {
            feedLikeRepository.delete(existing.get());
            isLiked = false;
        } else {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            feedLikeRepository.save(FeedLike.create(record, member));
            isLiked = true;

            Long recordOwnerId = record.getMember().getId();
            if (!recordOwnerId.equals(memberId)) {
                notificationService.send(
                        recordOwnerId,
                        NotificationType.FEED_LIKE,
                        member.getNickname() + "님이 회원님의 인증에 좋아요를 눌렀어요.",
                        "/feed#record-" + recordId
                );
            }
        }

        int count = feedLikeRepository.countByRecordId(recordId);
        return ToggleLikeResponse.of(isLiked, count);
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

        // 본인 인증에 댓글을 단 경우 알림 제외
        Long recordOwnerId = record.getMember().getId();
        if (!recordOwnerId.equals(memberId)) {
            notificationService.send(
                    recordOwnerId,
                    NotificationType.FEED_COMMENT,
                    member.getNickname() + "님이 회원님의 인증에 댓글을 달았어요.",
                    "/feed#record-" + recordId
            );
        }

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
