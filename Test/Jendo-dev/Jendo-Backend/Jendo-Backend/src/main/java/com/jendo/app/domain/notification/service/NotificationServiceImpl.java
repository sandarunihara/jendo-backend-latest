package com.jendo.app.domain.notification.service;

import com.jendo.app.common.dto.PaginationResponse;
import com.jendo.app.common.exceptions.NotFoundException;
import com.jendo.app.domain.notification.dto.NotificationReceiveDto;
import com.jendo.app.domain.notification.dto.NotificationRequestDto;
import com.jendo.app.domain.notification.dto.NotificationResponseDto;
import com.jendo.app.domain.notification.entity.Notification;
import com.jendo.app.domain.notification.mapper.NotificationMapper;
import com.jendo.app.domain.notification.repository.NotificationRepository;
import com.jendo.app.domain.user.entity.User;
import com.jendo.app.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationResponseDto createNotification(NotificationRequestDto request) {
        logger.info("Creating notification for user ID: {}", request.getUserId());
        
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User", request.getUserId()));
        
        Notification notification = notificationMapper.toEntity(request, user);
        notification = notificationRepository.save(notification);
        
        logger.info("Notification created with ID: {}", notification.getId());
        return notificationMapper.toResponseDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponseDto getNotificationById(Long id) {
        logger.info("Fetching notification with ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification", id));
        return notificationMapper.toResponseDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<NotificationResponseDto> getNotificationsByUserId(Long userId, int page, int size) {
        logger.info("Fetching notifications for user ID: {} - page: {}, size: {}", userId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notificationPage = notificationRepository.findByUserId(userId, pageable);
        
        List<NotificationResponseDto> content = notificationPage.getContent().stream()
                .map(notificationMapper::toResponseDto)
                .collect(Collectors.toList());
        
        return PaginationResponse.<NotificationResponseDto>builder()
                .content(content)
                .pageNumber(notificationPage.getNumber())
                .pageSize(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .first(notificationPage.isFirst())
                .last(notificationPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnreadNotificationsByUserId(Long userId) {
        logger.info("Fetching unread notifications for user ID: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        return notifications.stream().map(notificationMapper::toResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCountByUserId(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    public NotificationResponseDto markAsRead(Long id) {
        logger.info("Marking notification as read - ID: {}", id);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification", id));
        notification.setIsRead(true);
        notification = notificationRepository.save(notification);
        return notificationMapper.toResponseDto(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        logger.info("Marking all notifications as read for user ID: {}", userId);
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public void deleteNotification(Long id) {
        logger.info("Deleting notification with ID: {}", id);
        if (!notificationRepository.existsById(id)) {
            throw new NotFoundException("Notification", id);
        }
        notificationRepository.deleteById(id);
    }

    @Override
    public NotificationResponseDto saveReceivedNotification(NotificationReceiveDto request) {
        logger.info("Saving Firebase notification for user ID: {} - Message: {}", request.getUserId(), request.getMessage());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User", request.getUserId()));

        Notification notification = Notification.builder()
                .user(user)
                .message(request.getMessage())
                .type(request.getType() != null ? request.getType() : "SYSTEM")
                .isRead(false)
                .build();

        notification = notificationRepository.save(notification);

        logger.info("Firebase notification saved with ID: {} - FCM ID: {}", notification.getId(), request.getFcmMessageId());
        return notificationMapper.toResponseDto(notification);
    }
}
