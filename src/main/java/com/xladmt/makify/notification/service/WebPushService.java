package com.xladmt.makify.notification.service;

import com.xladmt.makify.common.constant.NotificationType;
import jakarta.annotation.PostConstruct;

public interface WebPushService {
    @PostConstruct
    void init() throws Exception;

    void sendToMember(Long memberId, NotificationType type, String body, String redirectUrl);
}
