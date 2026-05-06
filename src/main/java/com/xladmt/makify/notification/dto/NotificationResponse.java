package com.xladmt.makify.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xladmt.makify.common.constant.NotificationType;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.notification.domain.Notification;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class NotificationResponse {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final Long id;
    private final NotificationType type;
    private final String filterCategory;
    private final String iconClass;
    private final String message;
    private final String redirectUrl;

    // boolean + @Getter → isRead() → Jackson이 "read"로 직렬화하는 문제 방지
    @JsonProperty("isRead")
    private final boolean isRead;

    private final String createdAt;

    private NotificationResponse(Notification n) {
        this.id             = n.getId();
        this.type           = n.getType();
        this.filterCategory = n.getType().getFilterCategory();
        this.iconClass      = n.getType().getIconClass();
        this.message        = n.getMessage();
        this.redirectUrl    = n.getRedirectUrl();
        this.isRead         = YN.Y.equals(n.getIsRead());

        LocalDateTime createdAt = n.getCreatedAt();
        this.createdAt = createdAt != null ? createdAt.format(FORMATTER) : null;
    }

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n);
    }
}
