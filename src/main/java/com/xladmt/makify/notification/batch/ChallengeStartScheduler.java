package com.xladmt.makify.notification.batch;

import com.xladmt.makify.notification.service.ChallengeLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeStartScheduler {

    private final ChallengeLifecycleService lifecycleService;

//    /** 챌린지 시작 1시간 전 알림 */
//    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
//    public void notifyStart() {
//        LocalTime target = LocalTime.now()
//                .plusHours(1)
//                .truncatedTo(ChronoUnit.MINUTES);
//        lifecycleService.notifyStart(LocalDate.now(), target);
//    }
//
//    /** 인증 시작 10분 전 리마인더 */
//    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
//    public void notifyReminder() {
//        LocalTime target = LocalTime.now()
//                .plusMinutes(10)
//                .truncatedTo(ChronoUnit.MINUTES);
//        lifecycleService.notifyReminder(LocalDate.now(), target);
//    }
//
//    /** 챌린지 종료 다음날 17:00 성공/실패 판정 */
//    @Scheduled(cron = "0 0 17 * * *", zone = "Asia/Seoul")
//    public void finalizeEnded() {
//        LocalDate yesterday = LocalDate.now().minusDays(1);
//        lifecycleService.finalizeEnded(yesterday);
//    }

}
