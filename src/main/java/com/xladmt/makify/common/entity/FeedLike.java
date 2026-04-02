package com.xladmt.makify.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feed_like",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_feed_like",
                columnNames = {"record_id", "member_id"}
        ))
public class FeedLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_feed_like_record"))
    private ChallengeRecord record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_feed_like_member"))
    private Member member;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static FeedLike create(ChallengeRecord record, Member member) {
        FeedLike feedLike = new FeedLike();
        feedLike.record = record;
        feedLike.member = member;
        feedLike.createdAt = LocalDateTime.now();
        return feedLike;
    }
}
