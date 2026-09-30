package com.astrotech.transport.service;

import com.astrotech.transport.dto.response.NotificationResponse;
import com.astrotech.transport.entities.User;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.mappers.NotificationMapper;
import com.astrotech.transport.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;


    @Transactional
    public NotificationResponse create(
            User user,
            NotificationType type,
            String title,
            String message,
            String newActionUrl,
            UUID referenceId,
            NotificationReferenceType referenceType
    ) {
        var notificationMapper = NotificationMapper.createNotification(user, type, title, message, newActionUrl, referenceId, referenceType);

        var savedNotification = notificationRepository.save(notificationMapper);


        return NotificationMapper.notificationResponse(savedNotification);
    }


}
