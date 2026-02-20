package com.fitnessapp.notifications;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class FCMService {

    /**
     * Sends a push notification to a single FCM device token.
     * FirebaseMessaging.sendAsync() is a Future — we wrap it in a Mono using
     * fromFuture and subscribe on boundedElastic to avoid blocking the event loop.
     */
    public Mono<String> send(String fcmToken, String title, String body) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        return Mono.fromFuture(() -> FirebaseMessaging.getInstance().sendAsync(message))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(msgId -> log.debug("FCM message sent: messageId={}, token={}",
                        msgId, maskToken(fcmToken)))
                .doOnError(ex -> log.error("FCM send failed: token={}, error={}",
                        maskToken(fcmToken), ex.getMessage()));
    }

    /** Masks the FCM token for safe logging (shows first 8 and last 4 chars). */
    private String maskToken(String token) {
        if (token == null || token.length() < 12) return "****";
        return token.substring(0, 8) + "..." + token.substring(token.length() - 4);
    }
}
