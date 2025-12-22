package com.jendo.app.domain.jendotest.entity;

import com.jendo.app.domain.user.entity.User;
import com.jendo.app.domain.endotestreport.entity.EndoTestReport;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jendo_tests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"user", "endoTestReports"})
@ToString(exclude = {"user", "endoTestReports"})
public class JendoTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "risk_level", length = 50)
    private String riskLevel;

    @Column(name = "test_time")
    private LocalTime testTime;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    @Column(name = "spo2")
    private BigDecimal spo2;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "vascular_risk")
    private BigDecimal vascularRisk;

    @Column(name = "pdf_file_path", length = 500)
    private String pdfFilePath;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "jendoTest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EndoTestReport> endoTestReports = new ArrayList<>();
}
