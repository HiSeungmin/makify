package com.xladmt.makify.notification.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushSubscriptionRequest {
    private String endpoint;
    private Keys keys;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Keys {
        private String p256dh;
        private String auth;
    }
}
