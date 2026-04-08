package com.xladmt.makify.notification.repository;

import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 알림 목록 — 최신순, 무한스크롤
    Slice<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId, Pageable pageable);

    // 미읽음 수 — Redis 캐시 미스 시 폴백
    long countByReceiverIdAndIsRead(Long receiverId, YN isRead);

    // 전체 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = 'Y' " +
            "WHERE n.receiver.id = :receiverId AND n.isRead = 'N'")
    int markAllAsRead(@Param("receiverId") Long receiverId);

    // 단건 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = 'Y' " +
            "WHERE n.id = :id AND n.receiver.id = :receiverId AND n.isRead = 'N'")
    int markAsRead(@Param("id") Long id, @Param("receiverId") Long receiverId);
}
