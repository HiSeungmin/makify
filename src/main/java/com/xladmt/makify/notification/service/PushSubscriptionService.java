package com.xladmt.makify.notification.service;

import com.xladmt.makify.notification.dto.PushSubscriptionRequest;

public interface PushSubscriptionService {
    void subscribe(Long memberId, PushSubscriptionRequest request);
    void unsubscribe(String endpoint);
}

