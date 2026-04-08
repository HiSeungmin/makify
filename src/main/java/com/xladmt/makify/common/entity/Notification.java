package com.xladmt.makify.common.entity;

import com.xladmt.makify.common.constant.NotificationType;
import com.xladmt.makify.common.constant.YN;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notification",
        indexes = @Index(name = "idx_notification_receiver", columnList = "receiver_id, is_read, created_at"))
public class Notification extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String message;

    @Column(length = 500)
    private String redirectUrl;

    @Column(nullable = false)
    private YN isRead = YN.N;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Notification create(Member receiver,
                                      NotificationType type,
                                      String message,
                                      String redirectUrl) {
        Notification n = new Notification();
        n.receiver    = receiver;
        n.type        = type;
        n.message     = message;
        n.redirectUrl = redirectUrl;
        return n;
    }

    public void markAsRead() {
        this.isRead = YN.Y;
    }
}
