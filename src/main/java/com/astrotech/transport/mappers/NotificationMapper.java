package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.NotificationResponse;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.*;

import java.util.UUID;

public class NotificationMapper {
    public static Notification createNotification(
            User user,
            NotificationType type,
            String title,
            String message,
            String actionUrl,
            UUID referenceId,
            NotificationReferenceType referenceType
    ) {

        return Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
    }
    public static NotificationResponse notificationResponse(Notification notification) {

        return new NotificationResponse(
                notification.getId(),
                notification.getUser().getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getActionUrl(),
                notification.getReferenceId(),
                notification.getReferenceType(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
