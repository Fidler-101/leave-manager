package com.leavemanager.config;

import com.leavemanager.model.*;
import com.leavemanager.service.UserService;
import com.leavemanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final UserRepository userRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded, skipping initialization.");
            return;
        }

        log.info("Seeding database with demo data...");

        // Create employees
        var john = userService.createUser("John", "Doe", "john@company.com",
                "employee123", "Engineering", User.Role.EMPLOYEE);
        var sarah = userService.createUser("Sarah", "Connor", "sarah@company.com",
                "employee123", "Marketing", User.Role.EMPLOYEE);
        var bob = userService.createUser("Bob", "Johnson", "bob@company.com",
                "employee123", "Design", User.Role.EMPLOYEE);

        // Create managers
        var jane = userService.createUser("Jane", "Manager", "jane@company.com",
                "manager123", "Human Resources", User.Role.MANAGER);
        var mike = userService.createUser("Mike", "Grant", "mike@company.com",
                "manager123", "Operations", User.Role.MANAGER);

        // Seed some leave data for John — update his balance to show used days
        int year = LocalDate.now().getYear();
        leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(john,
                LeaveRequest.LeaveType.ANNUAL_LEAVE, year)
                .ifPresent(b -> { b.setUsed(7); leaveBalanceRepository.save(b); });
        leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(john,
                LeaveRequest.LeaveType.SICK_LEAVE, year)
                .ifPresent(b -> { b.setUsed(3); leaveBalanceRepository.save(b); });

        // Add past leave requests for John
        leaveRequestRepository.save(LeaveRequest.builder()
                .employee(john).reviewedBy(jane)
                .leaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.of(year - 1, 12, 10))
                .endDate(LocalDate.of(year - 1, 12, 12))
                .reason("Year-end break").status(LeaveRequest.Status.APPROVED)
                .build());
        leaveRequestRepository.save(LeaveRequest.builder()
                .employee(john).reviewedBy(jane)
                .leaveType(LeaveRequest.LeaveType.SICK_LEAVE)
                .startDate(LocalDate.of(year - 1, 11, 28))
                .endDate(LocalDate.of(year - 1, 11, 28))
                .reason("Flu").status(LeaveRequest.Status.APPROVED)
                .build());

        // Pending approvals
        leaveRequestRepository.save(LeaveRequest.builder()
                .employee(sarah)
                .leaveType(LeaveRequest.LeaveType.ANNUAL_LEAVE)
                .startDate(LocalDate.of(year, 12, 23))
                .endDate(LocalDate.of(year, 12, 27))
                .reason("Christmas break").status(LeaveRequest.Status.PENDING)
                .build());
        leaveRequestRepository.save(LeaveRequest.builder()
                .employee(bob)
                .leaveType(LeaveRequest.LeaveType.SICK_LEAVE)
                .startDate(LocalDate.of(year, 12, 20))
                .endDate(LocalDate.of(year, 12, 20))
                .reason("Doctor appointment").status(LeaveRequest.Status.PENDING)
                .build());

        log.info("✅ Database seeded successfully.");
        log.info("Demo accounts:");
        log.info("  Employee: john@company.com / employee123");
        log.info("  Employee: sarah@company.com / employee123");
        log.info("  Manager:  jane@company.com  / manager123");
    }
}
