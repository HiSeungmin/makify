package com.xladmt.makify.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xladmt.makify.common.constant.NotificationType;
import com.xladmt.makify.verification.domain.PushSubscription;
import com.xladmt.makify.notification.repository.PushSubscriptionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebPushServiceImpl implements WebPushService {

    @Value("${vapid.public-key}")
    private String publicKey;

    @Value("${vapid.private-key}")
    private String privateKey;

    private final PushSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    private PushService pushService;

    @PostConstruct
    @Override
    public void init() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        pushService = new PushService()
                .setPublicKey(publicKey)
                .setPrivateKey(privateKey);
    }

    @Override
    public void sendToMember(Long memberId, NotificationType type, String body, String redirectUrl) {
        List<PushSubscription> subs = subscriptionRepository.findByMemberId(memberId);
        if (subs.isEmpty()) return;

        String payload;
        try {
            payload = objectMapper.writeValueAsString(Map.of(
                    "title", type.getDefaultMessage(),
                    "body", body,
                    "url", redirectUrl != null ? redirectUrl : "/"
            ));
        } catch (Exception e) {
            log.error("[WebPush] payload 직렬화 실패 memberId={}", memberId, e);
            return;
        }

        for (PushSubscription sub : subs) {
            try {
                Notification notification = new Notification(
                        sub.getEndpoint(),
                        sub.getP256dh(),
                        sub.getAuth(),
                        payload.getBytes()
                );
                pushService.send(notification);
                log.debug("[WebPush] 발송 성공 memberId={} endpoint={}",
                        memberId, truncate(sub.getEndpoint()));
            } catch (Exception e) {
                handlePushError(sub, e);
            }
        }
    }

    private void handlePushError(PushSubscription sub, Exception e) {
        String msg = e.getMessage();
        // 410 Gone 또는 404 — 구독 만료/해제됨
        if (msg != null && (msg.contains("410") || msg.contains("404"))) {
            log.info("[WebPush] 만료된 구독 삭제 endpoint={}", truncate(sub.getEndpoint()));
            subscriptionRepository.delete(sub);
        } else {
            log.error("[WebPush] 발송 실패 endpoint={}", truncate(sub.getEndpoint()), e);
        }
    }

    private String truncate(String endpoint) {
        return endpoint.length() > 50 ? endpoint.substring(0, 50) + "..." : endpoint;
    }
}
