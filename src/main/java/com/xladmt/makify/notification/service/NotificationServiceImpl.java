package com.xladmt.makify.notification.service;

import com.xladmt.makify.common.constant.NotificationType;
import com.xladmt.makify.common.constant.YN;
import com.xladmt.makify.common.entity.Member;
import com.xladmt.makify.common.entity.Notification;
import com.xladmt.makify.common.exception.BusinessException;
import com.xladmt.makify.common.exception.ErrorCode;
import com.xladmt.makify.member.repository.MemberRepository;
import com.xladmt.makify.notification.SseEmitterManager;
import com.xladmt.makify.notification.dto.NotificationResponse;
import com.xladmt.makify.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
    private static final String UNREAD_KEY = "notification:unread:";

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final SseEmitterManager sseEmitterManager;
    private final StringRedisTemplate redisTemplate;
    private final WebPushService webPushService;

    @Override
    public SseEmitter subscribe(Long memberId) {
        SseEmitter emitter = sseEmitterManager.connect(memberId);
        // 연결 즉시 현재 미읽음 수 전송
        sseEmitterManager.send(memberId, "unread-count", getUnreadCount(memberId));
        return emitter;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(Long receiverId, NotificationType type, String message, String redirectUrl) {
        try {
            Member receiver = memberRepository.findById(receiverId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            Notification notification = Notification.create(receiver, type, message, redirectUrl);
            notificationRepository.save(notification);

            redisTemplate.opsForValue().increment(UNREAD_KEY + receiverId);

            if (sseEmitterManager.isConnected(receiverId)) {
                sseEmitterManager.send(receiverId, "notification", NotificationResponse.from(notification));
                sseEmitterManager.send(receiverId, "unread-count", getUnreadCount(receiverId));
            } else {
                webPushService.sendToMember(receiverId, type, message, redirectUrl);
            }

            log.debug("[Notification] type={}, receiverId={}", type, receiverId);

        } catch (Exception e) {
            log.error("[Notification] 알림 발송 실패 — type={}, receiverId={}, reason={}", type, receiverId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Slice<NotificationResponse> getNotifications(Long memberId, int page, int size) {
        return notificationRepository
                .findByReceiverIdAndIsDeletedOrderByCreatedAtDesc(memberId, YN.N, PageRequest.of(page, size))
                .map(NotificationResponse::from);
    }

    @Override
    public long getUnreadCount(Long memberId) {
        String cached = redisTemplate.opsForValue().get(UNREAD_KEY + memberId);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        long count = notificationRepository.countByReceiverIdAndIsRead(memberId, YN.N);
        redisTemplate.opsForValue().set(UNREAD_KEY + memberId, String.valueOf(count));
        return count;
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long memberId) {
        notificationRepository.markAsRead(notificationId, memberId);
        decrementUnreadCount(memberId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long memberId) {
        int updated = notificationRepository.markAllAsRead(memberId);
        if (updated > 0) {
            redisTemplate.delete(UNREAD_KEY + memberId);
        }
    }

    private void decrementUnreadCount(Long memberId) {
        String key    = UNREAD_KEY + memberId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            long current = Long.parseLong(cached);
            if (current > 0) {
                redisTemplate.opsForValue().set(key, String.valueOf(current - 1));
            }
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean deleteNotification(Long notificationId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Notification notification = notificationRepository.findByIdAndReceiverId(notificationId, member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        boolean needMarkAsRead = false;

        if(notification.getIsRead().equals(YN.N)) needMarkAsRead = true;
        notification.markAsDeleted();

        return needMarkAsRead;
    }
}
