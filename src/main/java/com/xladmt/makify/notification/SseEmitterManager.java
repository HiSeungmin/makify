package com.xladmt.makify.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SseEmitterManager {
    // Nginx 기본 read_timeout(60s)보다 길게 설정
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(Long memberId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() -> emitters.remove(memberId));
        emitter.onTimeout(()    -> emitters.remove(memberId));
        emitter.onError(e       -> emitters.remove(memberId));

        emitters.put(memberId, emitter);

        // 연결 직후 더미 이벤트 — 브라우저 SSE 버퍼 확정용
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            emitters.remove(memberId);
        }

        log.debug("[SSE] connected memberId={}, total={}", memberId, emitters.size());
        return emitter;
    }

    public void send(Long memberId, String eventName, Object data) {
        SseEmitter emitter = emitters.get(memberId);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            emitters.remove(memberId);
            log.debug("[SSE] send failed, removed memberId={}", memberId);
        }
    }

    public boolean isConnected(Long memberId) {
        return emitters.containsKey(memberId);
    }
}
