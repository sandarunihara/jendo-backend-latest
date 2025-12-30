package com.jendo.app.domain.notification.repository;

import com.jendo.app.domain.notification.entity.ScheduledNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduledNotificationRepository extends JpaRepository<ScheduledNotification, Long> {

    List<ScheduledNotification> findByScheduledForBeforeAndSentFalse(LocalDateTime now);

    void deleteByAppointment_Id(Long appointmentId);
}
