package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.YN;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ChallengeRecord")
public class ChallengeRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_challenge_record_member"))
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_challenge_record_challenge"))
    private Challenge challenge;

    @Column(name = "verificated_date", nullable = false)
    private LocalDateTime verificatedDate;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "memo", length = 1000)
    private String memo;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_approved")
    private YN isApproved;

    @Column(name = "refusal_reason", length = 1000)
    private String refusalReason;

    @Enumerated(EnumType.STRING)
    private YN isPublic;

    @Enumerated(EnumType.STRING)
    private YN isVisible;

    private Long manager;

    public static ChallengeRecord create(Member member, Challenge challenge, String imageUrl, String memo, YN isPublic) {
        ChallengeRecord record = new ChallengeRecord();
        record.member = member;
        record.challenge = challenge;
        record.imageUrl = imageUrl;
        record.memo = memo;
        record.verificatedDate = LocalDateTime.now();
        record.isPublic = isPublic;
        record.isApproved = YN.Y;
        record.isVisible = YN.Y;
        return record;
    }

    public void delete() {
        this.isVisible = YN.N;
    }

    public void togglePublic() {
        this.isPublic = YN.Y.equals(this.isPublic) ? YN.N : YN.Y;
    }
}
