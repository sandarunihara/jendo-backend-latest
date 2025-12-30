package com.jendo.app.domain.notification.mapper;

import com.jendo.app.domain.notification.dto.NotificationReceiveDto;
import com.jendo.app.domain.notification.dto.NotificationRequestDto;
import com.jendo.app.domain.notification.dto.NotificationResponseDto;
import com.jendo.app.domain.notification.entity.Notification;
import com.jendo.app.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponseDto toResponseDto(Notification notification) {
        if (notification == null) return null;

        return NotificationResponseDto.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public Notification toEntity(NotificationRequestDto dto, User user) {
        if (dto == null) return null;

        return Notification.builder()
                .user(user)
                .message(dto.getMessage())
                .type(dto.getType())
                .isRead(false)
                .build();
    }

    public Notification toEntity(NotificationReceiveDto dto, User user) {
        if (dto == null) return null;

        return Notification.builder()
                .user(user)
                .message(dto.getMessage())
                .type(dto.getType() != null ? dto.getType() : "SYSTEM")
                .isRead(false)
                .build();
    }
}