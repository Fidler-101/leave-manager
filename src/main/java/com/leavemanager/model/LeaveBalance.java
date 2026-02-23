package com.leavemanager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_balances",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "leave_type", "year"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveRequest.LeaveType leaveType;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    @Builder.Default
    private int allocated = 0;

    @Column(nullable = false)
    @Builder.Default
    private int used = 0;

    public int getRemaining() {
        return allocated - used;
    }
}
