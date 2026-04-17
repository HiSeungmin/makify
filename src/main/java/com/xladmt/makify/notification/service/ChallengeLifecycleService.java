package com.xladmt.makify.notification.service;

import com.xladmt.makify.common.constant.Frequency;
import com.xladmt.makify.common.entity.UserChallenge;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

public interface ChallengeLifecycleService {
    void notifyStart(LocalDate today, LocalTime targetTime);
    void notifyReminder(LocalDate today, LocalTime targetTime);
    void finalizeEnded(LocalDate endedOn);
}
