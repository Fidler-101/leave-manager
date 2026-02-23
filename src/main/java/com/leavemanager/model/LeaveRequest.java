package com.leavemanager.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 500)
    private String reason;

    private String attachmentPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.PENDING;

    private String managerComment;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime reviewedAt;

    public int getNumberOfDays() {
        if (startDate == null || endDate == null) return 0;
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    public enum LeaveType {
        ANNUAL_LEAVE("Annual Leave"),
        SICK_LEAVE("Sick Leave"),
        PARENTAL_LEAVE("Parental Leave"),
        EMERGENCY_LEAVE("Emergency Leave"),
        UNPAID_LEAVE("Unpaid Leave");

        private final String displayName;
        LeaveType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    public enum Status {
        PENDING, APPROVED, REJECTED, CANCELLED
    }
}
