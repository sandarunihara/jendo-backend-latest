package com.jendo.app.domain.user.entity;

import com.jendo.app.domain.appointment.entity.Appointment;
import com.jendo.app.domain.healthparameter.entity.HealthParameter;
import com.jendo.app.domain.jendotest.entity.JendoTest;
import com.jendo.app.domain.notification.entity.Notification;
import com.jendo.app.domain.endotestreport.entity.EndoTestReport;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"roles", "jendoTests", "appointments", "notifications", "healthParameters", "endoTestReports"})
@ToString(exclude = {"roles", "jendoTests", "appointments", "notifications", "healthParameters", "endoTestReports"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "address")
    private String address;
    
    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "Asia/Colombo";  // Default to Sri Lanka timezone
    
    @Column(name = "weight")
    private Double weight;
    
    @Column(name = "height")
    private Double height;
    
    @Column(name = "auth_provider", length = 20)
    @Builder.Default
    private String authProvider = "local";
    
    @Column(name = "google_id", unique = true)
    private String googleId;
    
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "created_by")
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Role> roles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JendoTest> jendoTests = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HealthParameter> healthParameters = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EndoTestReport> endoTestReports = new ArrayList<>();
}
