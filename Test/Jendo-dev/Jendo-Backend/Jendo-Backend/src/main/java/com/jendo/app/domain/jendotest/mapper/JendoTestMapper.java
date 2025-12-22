package com.jendo.app.domain.jendotest.mapper;

import com.jendo.app.domain.jendotest.dto.JendoTestRequestDto;
import com.jendo.app.domain.jendotest.dto.JendoTestResponseDto;
import com.jendo.app.domain.jendotest.entity.JendoTest;
import com.jendo.app.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class JendoTestMapper {

    public JendoTest toEntity(JendoTestRequestDto dto, User user) {
        return JendoTest.builder()
                .user(user)
                .score(dto.getScore())
                .heartRate(dto.getHeartRate())
                .riskLevel(dto.getRiskLevel())
                .testTime(dto.getTestTime())
                .bloodPressure(dto.getBloodPressure())
                .spo2(dto.getSpo2())
                .testDate(dto.getTestDate())
                .vascularRisk(dto.getVascularRisk())
                .pdfFilePath(dto.getPdfFilePath())
                .build();
    }

    public JendoTestResponseDto toResponseDto(JendoTest entity) {
        return JendoTestResponseDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .userName(entity.getUser().getFirstName() + " " + entity.getUser().getLastName())
                .score(entity.getScore())
                .heartRate(entity.getHeartRate())
                .riskLevel(entity.getRiskLevel())
                .testTime(entity.getTestTime())
                .bloodPressure(entity.getBloodPressure())
                .spo2(entity.getSpo2())
                .testDate(entity.getTestDate())
                .vascularRisk(entity.getVascularRisk())
                .pdfFilePath(entity.getPdfFilePath())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
