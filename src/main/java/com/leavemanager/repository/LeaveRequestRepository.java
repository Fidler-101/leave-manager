package com.leavemanager.repository;

import com.leavemanager.model.LeaveRequest;
import com.leavemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeOrderByCreatedAtDesc(User employee);

    List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveRequest.Status status);

    List<LeaveRequest> findByEmployeeAndStatusOrderByCreatedAtDesc(User employee, LeaveRequest.Status status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING' ORDER BY lr.createdAt ASC")
    List<LeaveRequest> findAllPending();

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee = :employee AND lr.startDate >= :startDate AND lr.endDate <= :endDate")
    List<LeaveRequest> findByEmployeeAndDateRange(@Param("employee") User employee,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'APPROVED' AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findApprovedLeavesInRange(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    long countByEmployeeAndStatus(User employee, LeaveRequest.Status status);
}
