package com.astrotech.transport.notifications;


import com.astrotech.transport.entities.User;
import com.astrotech.transport.enums.NotificationReferenceType;
import com.astrotech.transport.enums.NotificationType;
import com.astrotech.transport.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WebsocketNotifications {
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;


    public void sendNotifications(User user,
                                  NotificationType type,
                                  String title,
                                  String message,
                                  String actionUrl,
                                  UUID referenceId,
                                  NotificationReferenceType referenceType) {
        var response = notificationService.create(user, type, title, message, actionUrl, referenceId, referenceType);
        if (response != null) {
            messagingTemplate.convertAndSendToUser(
                    response.userId().toString(),
                    "/queue/notifications",
                    response
            );
        }

    }
}
