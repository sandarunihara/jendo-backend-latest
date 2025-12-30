package com.jendo.app.domain.appointment.service;

import com.jendo.app.common.dto.PaginationResponse;
import com.jendo.app.domain.appointment.dto.AppointmentRequestDto;
import com.jendo.app.domain.appointment.dto.AppointmentResponseDto;

public interface AppointmentService {

    AppointmentResponseDto createAppointment(AppointmentRequestDto request);

    AppointmentResponseDto getAppointmentById(Long id);

    PaginationResponse<AppointmentResponseDto> getAllAppointments(int page, int size);

    PaginationResponse<AppointmentResponseDto> getAppointmentsByUserId(Long userId, int page, int size);

    PaginationResponse<AppointmentResponseDto> getAppointmentsByDoctorId(Long doctorId, int page, int size);

    AppointmentResponseDto updateAppointment(Long id, AppointmentRequestDto request);

    AppointmentResponseDto updateAppointmentStatus(Long id, String status);

    void deleteAppointment(Long id);
}