package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.NotificationReferenceType;
import com.astrotech.transport.enums.NotificationType;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID userId,
        NotificationType notificationType,
        String title,
        String message,
        String actionUrl,
        UUID referenceId,
        NotificationReferenceType notificationReferenceType,
        Boolean read,
        Instant createdAt

) {
}
