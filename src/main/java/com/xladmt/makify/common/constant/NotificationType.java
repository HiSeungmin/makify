package com.xladmt.makify.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    // 챌린지 탭
    VERIFICATION_APPROVED("challenge", "bi-check-circle",  "인증이 완료되었습니다."),
    VERIFICATION_REJECTED("challenge", "bi-x-circle",      "인증이 거부되었습니다."),
    CHALLENGE_SUCCESS    ("challenge", "bi-trophy",         "챌린지를 성공했습니다!"),
    CHALLENGE_FAIL       ("challenge", "bi-emoji-frown",   "챌린지가 실패로 종료되었습니다."),
    CHALLENGE_START      ("challenge", "bi-alarm",         "챌린지가 시작되었습니다."),
    REMINDER             ("challenge", "bi-alarm",         "챌린지 인증 시간이 다가왔습니다."),

    // 결제 탭
    PAYMENT_COMPLETE     ("payment",   "bi-credit-card",   "챌린지 참여 결제가 완료되었습니다."),
    PAYMENT_FAIL         ("payment",   "bi-credit-card-2-back", "결제에 실패했습니다. 다시 시도해 주세요."),
    REFUND_COMPLETE      ("payment",   "bi-arrow-counterclockwise", "환불이 완료되었습니다."),
    PAYOUT_COMPLETE      ("payment",   "bi-cash-stack",    "챌린지 예치금이 환급되었습니다."),

    // 커뮤니티 탭
    FEED_LIKE            ("community", "bi-heart",         "회원님의 인증에 좋아요가 달렸습니다."),
    FEED_COMMENT         ("community", "bi-chat-dots",     "회원님의 인증에 댓글이 달렸습니다."),


    // 시스템 탭
    SYSTEM               ("system",    "bi-gear",          "시스템 알림입니다.");

    /** notifications.html 의 data-filter 값과 일치 */
    private final String filterCategory;

    /** Bootstrap Icons 클래스명 */
    private final String iconClass;

    /** 기본 메시지 */
    private final String defaultMessage;
}
