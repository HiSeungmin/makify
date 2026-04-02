package com.xladmt.makify.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Review",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_review_member_challenge",
                columnNames = {"member_id", "challenge_id"}
        ))
public class Review extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_challenge"))
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_member"))
    private Member member;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "star", nullable = false)
    private Integer star;

    private LocalDateTime createDate;
    private LocalDateTime updateDate;

    public static Review create(Challenge challenge, Member member, String content, Integer star) {
        Review review = new Review();
        review.challenge = challenge;
        review.member = member;
        review.content = content;
        review.star = star;
        review.createDate = LocalDateTime.now();
        review.updateDate = LocalDateTime.now();
        return review;
    }

    public void update(String content, Integer star) {
        this.content = content;
        this.star = star;
        this.updateDate = LocalDateTime.now();
    }
}
