package com.jendo.app.domain.wellnessrecommendation.repository;

import com.jendo.app.domain.wellnessrecommendation.entity.DailyAiTip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DailyAiTipRepository extends JpaRepository<DailyAiTip, Long> {

    Optional<DailyAiTip> findFirstByUserIdAndWindowStartLessThanEqualAndWindowEndGreaterThanEqual(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Modifying
    @Transactional
    @Query("delete from DailyAiTip t where t.windowEnd < :now")
    void deleteExpired(@Param("now") LocalDateTime now);
}
