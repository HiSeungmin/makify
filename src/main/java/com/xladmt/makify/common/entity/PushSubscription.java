package com.xladmt.makify.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "push_subscription")
public class PushSubscription extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(nullable = false, length = 200)
    private String p256dh;

    @Column(nullable = false, length = 200)
    private String auth;

    public static PushSubscription create(Member member, String endpoint,
                                          String p256dh, String auth) {
        PushSubscription sub = new PushSubscription();
        sub.member   = member;
        sub.endpoint = endpoint;
        sub.p256dh   = p256dh;
        sub.auth     = auth;
        return sub;
    }

    public void updateKeys(String p256dh, String auth) {
        this.p256dh = p256dh;
        this.auth   = auth;
    }
}
