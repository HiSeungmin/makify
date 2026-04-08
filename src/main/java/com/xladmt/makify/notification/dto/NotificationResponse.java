package com.xladmt.makify.notification.dto;

import com.xladmt.makify.common.constant.NotificationType;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.Notification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {
    private final Long id;
    private final NotificationType type;
    private final String filterCategory;  // notifications.html data-filter 값
    private final String iconClass;       // Bootstrap Icons 클래스명
    private final String message;
    private final String redirectUrl;
    private final boolean isRead;
    private final LocalDateTime createdAt;

    private NotificationResponse(Notification n) {
        this.id             = n.getId();
        this.type           = n.getType();
        this.filterCategory = n.getType().getFilterCategory();
        this.iconClass      = n.getType().getIconClass();
        this.message        = n.getMessage();
        this.redirectUrl    = n.getRedirectUrl();
        this.isRead         = YN.Y.equals(n.getIsRead());
        this.createdAt      = n.getCreatedAt();
    }

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n);
    }

}
