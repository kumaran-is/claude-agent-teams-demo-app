package com.fitnessapp.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalTime;

/**
 * Hourly cron job that dispatches workout reminder push notifications to users
 * whose reminder_time matches the current hour and minute.
 *
 * Runs every minute to match users' exact reminder_time settings.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationPreferenceRepository preferenceRepository;
    private final FCMService fcmService;

    @Scheduled(cron = "0 * * * * *")  // every minute
    public void dispatchReminders() {
        LocalTime now = LocalTime.now(java.time.ZoneOffset.UTC).withSecond(0).withNano(0);
        log.debug("Checking reminders for time: {}", now);

        preferenceRepository.findDueReminders(now)
                .flatMap(pref ->
                        fcmService.send(
                                pref.getFcmToken(),
                                "Time to Work Out!",
                                "Your daily workout reminder. Stay consistent and reach your goals!"
                        )
                        .onErrorResume(ex -> {
                            log.error("Failed to send reminder to userId={}: {}",
                                    pref.getUserId(), ex.getMessage());
                            return Mono.empty();
                        })
                )
                .subscribe(
                        msgId -> log.debug("Reminder dispatched: messageId={}", msgId),
                        ex -> log.error("Scheduler error during reminder dispatch", ex),
                        () -> log.debug("Reminder dispatch cycle complete for time={}", now)
                );
    }
}
