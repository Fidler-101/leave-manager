package com.leavemanager.service;

import com.leavemanager.model.*;
import com.leavemanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final UserRepository userRepository;

    // ── Employee Operations ───────────────────────────────────────────────────

    public List<LeaveRequest> getMyRequests(User employee) {
        return leaveRequestRepository.findByEmployeeOrderByCreatedAtDesc(employee);
    }

    public List<LeaveBalance> getMyBalances(User employee) {
        int year = LocalDate.now().getYear();
        return leaveBalanceRepository.findByEmployeeAndYear(employee, year);
    }

    @Transactional
    public LeaveRequest submitRequest(User employee, LeaveRequest.LeaveType leaveType,
                                       LocalDate startDate, LocalDate endDate,
                                       String reason, String attachmentPath) {
        // Check balance
        int year = LocalDate.now().getYear();
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository
                .findByEmployeeAndLeaveTypeAndYear(employee, leaveType, year);

        int days = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;

        if (leaveType != LeaveRequest.LeaveType.SICK_LEAVE
                && leaveType != LeaveRequest.LeaveType.EMERGENCY_LEAVE
                && leaveType != LeaveRequest.LeaveType.UNPAID_LEAVE) {
            if (balanceOpt.isEmpty() || balanceOpt.get().getRemaining() < days) {
                throw new IllegalArgumentException("Insufficient leave balance for " + leaveType.getDisplayName());
            }
        }

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(startDate)
                .endDate(endDate)
                .reason(reason)
                .attachmentPath(attachmentPath)
                .status(LeaveRequest.Status.PENDING)
                .build();

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public void cancelRequest(Long requestId, User employee) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!request.getEmployee().getId().equals(employee.getId())) {
            throw new IllegalArgumentException("You can only cancel your own requests");
        }
        if (request.getStatus() != LeaveRequest.Status.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be cancelled");
        }
        request.setStatus(LeaveRequest.Status.CANCELLED);
        leaveRequestRepository.save(request);
    }

    // ── Manager Operations ────────────────────────────────────────────────────

    public List<LeaveRequest> getAllPendingRequests() {
        return leaveRequestRepository.findAllPending();
    }

    public List<LeaveRequest> getAllRequests() {
        return leaveRequestRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void approveRequest(Long requestId, User manager, String comment) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        request.setStatus(LeaveRequest.Status.APPROVED);
        request.setReviewedBy(manager);
        request.setManagerComment(comment);
        request.setReviewedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);

        // Deduct from balance
        int year = request.getStartDate().getYear();
        leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(
                request.getEmployee(), request.getLeaveType(), year)
                .ifPresent(balance -> {
                    balance.setUsed(balance.getUsed() + request.getNumberOfDays());
                    leaveBalanceRepository.save(balance);
                });
    }

    @Transactional
    public void rejectRequest(Long requestId, User manager, String comment) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        request.setStatus(LeaveRequest.Status.REJECTED);
        request.setReviewedBy(manager);
        request.setManagerComment(comment);
        request.setReviewedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
    }

    // ── Approved leaves for calendar ─────────────────────────────────────────

    public List<LeaveRequest> getApprovedLeavesForMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return leaveRequestRepository.findApprovedLeavesInRange(start, end);
    }
}
