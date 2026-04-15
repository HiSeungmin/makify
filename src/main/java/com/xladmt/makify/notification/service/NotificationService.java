package com.xladmt.makify.notification.service;

import com.xladmt.makify.common.constant.NotificationType;
import com.xladmt.makify.notification.dto.NotificationResponse;
import org.springframework.data.domain.Slice;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationService {
    SseEmitter subscribe(Long memberId);
    void send(Long receiverId, NotificationType type, String message, String redirectUrl);
    Slice<NotificationResponse> getNotifications(Long memberId, int page, int size);
    long getUnreadCount(Long memberId);
    void markAsRead(Long notificationId, Long memberId);
    void markAllAsRead(Long memberId);
    boolean deleteNotification(Long notificationId, Long memberId);
}
