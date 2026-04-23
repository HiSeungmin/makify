package com.xladmt.makify.notification.controller;

import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.notification.dto.PushSubscriptionRequest;
import com.xladmt.makify.notification.service.PushSubscriptionService;
import com.xladmt.makify.notification.service.WebPushService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class PushController {

    @Value("${vapid.public-key}")
    private String vapidPublicKey;

    private final WebPushService webPushService;
    private final PushSubscriptionService pushSubscriptionService;

    @GetMapping("/vapid-key")
    public ResponseEntity<Map<String, String>> getVapidKey() {
        return ResponseEntity.ok(Map.of("publicKey", vapidPublicKey));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestBody PushSubscriptionRequest request) {
        pushSubscriptionService.subscribe(memberDetails.getId(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<Void> unsubscribe(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestBody Map<String, String> body) {
        pushSubscriptionService.unsubscribe(body.get("endpoint"));
        return ResponseEntity.noContent().build();
    }

}
