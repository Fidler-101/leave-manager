package com.leavemanager.repository;

import com.leavemanager.model.LeaveBalance;
import com.leavemanager.model.LeaveRequest;
import com.leavemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, Long> {
    List<LeaveBalance> findByEmployeeAndYear(User employee, int year);
    Optional<LeaveBalance> findByEmployeeAndLeaveTypeAndYear(User employee, LeaveRequest.LeaveType leaveType, int year);
    List<LeaveBalance> findByEmployee(User employee);
}
