package org.example.api_gateway.config;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class NotificationDto {
    private Long id;

    private String title;

    private String link;

    private String message;

    private boolean checked;

    private String accountEmail;

    private String studyPath;

    private NotificationType notificationType;
}

