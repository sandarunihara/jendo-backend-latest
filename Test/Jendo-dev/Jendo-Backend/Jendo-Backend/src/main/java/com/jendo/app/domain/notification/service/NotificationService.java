package com.jendo.app.domain.notification.service;

import com.jendo.app.common.dto.PaginationResponse;
import com.jendo.app.domain.notification.dto.NotificationReceiveDto;
import com.jendo.app.domain.notification.dto.NotificationRequestDto;
import com.jendo.app.domain.notification.dto.NotificationResponseDto;

import java.util.List;

public interface NotificationService {
    
    NotificationResponseDto createNotification(NotificationRequestDto request);
    
    NotificationResponseDto getNotificationById(Long id);
    
    PaginationResponse<NotificationResponseDto> getNotificationsByUserId(Long userId, int page, int size);
    
    List<NotificationResponseDto> getUnreadNotificationsByUserId(Long userId);
    
    long getUnreadCountByUserId(Long userId);
    
    NotificationResponseDto markAsRead(Long id);
    
    void markAllAsRead(Long userId);
    
    void deleteNotification(Long id);

    NotificationResponseDto saveReceivedNotification(NotificationReceiveDto request);
}
