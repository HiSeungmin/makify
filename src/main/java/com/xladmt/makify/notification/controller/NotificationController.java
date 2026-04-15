package com.xladmt.makify.notification.controller;


import com.xladmt.makify.common.config.security.MemberDetails;
import com.xladmt.makify.notification.dto.NotificationResponse;
import com.xladmt.makify.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // SSE 구독 — 로그인 후 최초 1회 호출
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal MemberDetails memberDetails) {
        return notificationService.subscribe(memberDetails.getId());
    }

    // 알림 목록 (무한스크롤)
    @GetMapping
    public ResponseEntity<Slice<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                notificationService.getNotifications(memberDetails.getId(), page, size));
    }

    // 미읽음 수
    @GetMapping("/count")
    public ResponseEntity<Long> getUnreadCount(
            @AuthenticationPrincipal MemberDetails memberDetails) {
        return ResponseEntity.ok(notificationService.getUnreadCount(memberDetails.getId()));
    }

    // 단건 읽음 처리
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @PathVariable Long id) {
        notificationService.markAsRead(id, memberDetails.getId());
        return ResponseEntity.noContent().build();
    }

    // 전체 읽음 처리
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal MemberDetails memberDetails) {
        notificationService.markAllAsRead(memberDetails.getId());
        return ResponseEntity.noContent().build();
    }

    // 알림 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal MemberDetails memberDetails,
                                       @PathVariable Long id) {
        boolean needMarkAsRead = notificationService.deleteNotification(id, memberDetails.getId());
        if(needMarkAsRead) {notificationService.markAsRead(id, memberDetails.getId());}

        return ResponseEntity.noContent().build();
    }
}
