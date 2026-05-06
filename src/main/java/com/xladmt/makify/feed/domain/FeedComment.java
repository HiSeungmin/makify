package com.xladmt.makify.feed.domain;

import com.xladmt.makify.challenge.domain.ChallengeRecord;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "feed_comment")
public class FeedComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_feed_comment_record"))
    private ChallengeRecord record;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_feed_comment_member"))
    private Member member;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_visible", nullable = false)
    private YN isVisible;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static FeedComment create(ChallengeRecord record, Member member, String content) {
        FeedComment comment = new FeedComment();
        comment.record = record;
        comment.member = member;
        comment.content = content;
        comment.isVisible = YN.Y;
        comment.createdAt = LocalDateTime.now();
        comment.updatedAt = LocalDateTime.now();
        return comment;
    }

    public void delete() {
        this.isVisible = YN.N;
    }

    public void update(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }
}
