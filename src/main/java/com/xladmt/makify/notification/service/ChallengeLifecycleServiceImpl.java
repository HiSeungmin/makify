package com.xladmt.makify.notification.service;

import com.xladmt.makify.common.constant.NotificationType;
import com.xladmt.makify.common.entity.UserChallenge;
import com.xladmt.makify.verification.repository.ChallengeRecordRepository;
import com.xladmt.makify.challenge.repository.UserChallengeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xladmt.makify.common.constant.Frequency;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeLifecycleServiceImpl implements ChallengeLifecycleService {
    private final UserChallengeRepository userChallengeRepository;
    private final ChallengeRecordRepository challengeRecordRepository;
    private final NotificationService notificationService;


    /**
     * 리마인더 알림.
     * 기간 중, startTime-10min에 해당하는 챌린지의 JOINED 사용자에게 발송.
     * Frequency에 따라 분기 처리.
     */
    @Transactional(readOnly = true)
    @Override
    public void notifyReminder(LocalDate today, LocalTime targetTime) {
        List<UserChallenge> candidates =
                userChallengeRepository.findJoinedForReminder(today, targetTime);

        if (candidates.isEmpty()) return;

        log.info("[ReminderScheduler] candidate={}, today={}, targetTime={}",
                candidates.size(), today, targetTime);

        int sent = 0;
        for (UserChallenge uc : candidates) {
            if (!shouldSendReminderToday(uc, today)) continue;

            sendSafely(uc, NotificationType.REMINDER,
                    String.format("10분 뒤 <%s> 챌린지를 인증하세요", uc.getChallenge().getTitle()),
                    "/challenge/" + uc.getChallenge().getId());
            sent++;
        }
        log.info("[ReminderScheduler] sent={}", sent);
    }

    /**
     * 챌린지 시작 알림.
     * startDate=today이고 startTime-1h에 해당하는 챌린지의 JOINED 사용자에게 발송.
     */
    @Transactional(readOnly = true)
    @Override
    public void notifyStart(LocalDate today, LocalTime targetTime) {
        List<UserChallenge> targets =
                userChallengeRepository.findJoinedForChallengeStart(today, targetTime);

        if (targets.isEmpty()) return;

        log.info("[StartScheduler] target={}, today={}, targetTime={}",
                targets.size(), today, targetTime);

        for (UserChallenge uc : targets) {
            sendSafely(uc, NotificationType.CHALLENGE_START,
                    String.format("곧 <%s> 챌린지가 시작해요", uc.getChallenge().getTitle()),
                    "/challenge/" + uc.getChallenge().getId());
        }
    }

    /**
     * 종료 판정. endDate = endedOn 인 JOINED 사용자를 성공/실패 처리.
     * 상태 전이 자체가 멱등 가드. 알림은 커밋 후 이벤트로 발송 권장(여기선 단순화).
     */
    @Transactional
    @Override
    public void finalizeEnded(LocalDate endedOn) {
        List<UserChallenge> targets =
                userChallengeRepository.findJoinedForFinalization(endedOn);

        if (targets.isEmpty()) return;

        log.info("[CompletionScheduler] target={}, endedOn={}",
                targets.size(), endedOn);

        for (UserChallenge uc : targets) {
            boolean success = isSuccess(uc);

            if (success) {
                uc.markAsCompleted();
                sendSafely(uc, NotificationType.CHALLENGE_SUCCESS,
                        String.format("<%s> 챌린지 결과가 나왔어요. 확인해보세요",
                                uc.getChallenge().getTitle()),
                        "/challenge/" + uc.getChallenge().getId());
            } else {
                uc.markAsFailed();
                sendSafely(uc, NotificationType.CHALLENGE_FAIL,
                        String.format("<%s> 챌린지 결과가 나왔어요. 확인해보세요",
                                uc.getChallenge().getTitle()),
                        "/challenge/" + uc.getChallenge().getId());
            }
        }
        // JPA dirty checking으로 상태 UPDATE는 트랜잭션 종료 시 자동 반영
    }

    /**
     * Frequency 분기 판정.
     *  - DAILY / CUSTOM: 항상 발송
     *  - WEEKDAYS: 평일에만 발송
     *  - WEEKENDS: 주말에만 발송
     *  - N_PER_WEEK: 이번 주(월~일) 인증 횟수 < countPerWeek 일 때만
     */
     private boolean shouldSendReminderToday(UserChallenge uc, LocalDate today) {
        Frequency freq = uc.getChallenge().getVerificationMethod().getFrequency();
        DayOfWeek dow = today.getDayOfWeek();

        switch (freq) {
            case DAILY:
            case CUSTOM:
                return true;

            case WEEKDAYS:
                return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;

            case WEEKENDS:
                return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;

            case ONE_PER_WEEK:
            case TWO_PER_WEEK:
            case THREE_PER_WEEK:
            case FOUR_PER_WEEK:
            case FIVE_PER_WEEK:
            case SIX_PER_WEEK:
                return !hasReachedWeeklyGoal(uc, today, freq.getCountPerWeek());

            default:
                return true;
        }
    }

    private boolean hasReachedWeeklyGoal(UserChallenge uc, LocalDate today, int goal) {
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = monday.plusDays(7).atStartOfDay();

        long count = challengeRecordRepository.countInWeek(
                uc.getMember().getId(), uc.getChallenge().getId(), weekStart, weekEnd);
        return count >= goal;
    }

    private boolean isSuccess(UserChallenge uc) {
        Integer target = uc.getTargetFrequency();
        Integer actual = uc.getActualFrequency();
        if (target == null || actual == null) {
            log.warn("[CompletionScheduler] null frequency uc={} target={} actual={}",
                    uc.getId(), target, actual);
            return false;
        }
        return actual >= target;
    }

    private void sendSafely(UserChallenge uc, NotificationType type,
                            String message, String redirectUrl) {
        try {
            notificationService.send(
                    uc.getMember().getId(), type, message, redirectUrl);
        } catch (Exception e) {
            log.error("[LifecycleService] 알림 발송 실패 type={} ucId={}",
                    type, uc.getId(), e);
        }
    }
}
