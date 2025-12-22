package com.jendo.app.domain.jendotest.repository;

import com.jendo.app.domain.jendotest.entity.JendoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JendoTestRepository extends JpaRepository<JendoTest, Long> {
    
    Page<JendoTest> findByUserId(Long userId, Pageable pageable);
    
    List<JendoTest> findByUserIdAndTestDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<JendoTest> findByRiskLevel(String riskLevel);
    
    Optional<JendoTest> findFirstByUserIdOrderByTestDateDescCreatedAtDesc(Long userId);
}
