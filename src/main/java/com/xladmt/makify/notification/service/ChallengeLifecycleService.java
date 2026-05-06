package com.xladmt.makify.notification.service;

import java.time.LocalDate;
import java.time.LocalTime;

public interface ChallengeLifecycleService {
    void notifyStart(LocalDate today, LocalTime targetTime);
    void notifyReminder(LocalDate today, LocalTime targetTime);
    void finalizeEnded(LocalDate endedOn);
}
