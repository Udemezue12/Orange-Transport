package com.astrotech.transport.notifications;

public record EmailNotificationResponse(
        String html,
        String text
) {
}
