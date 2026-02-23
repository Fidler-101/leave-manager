package com.leavemanager.service;

import com.leavemanager.model.*;
import com.leavemanager.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeaveServiceTest {

    @Mock
    private LeaveBalanceRepository leaveBalanceRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks
    private LeaveService leaveService;

    @Test
    void testSubmitRequest_InsufficientBalance() {
        // Arrange
        User employee = new User();
        employee.setId(1L);

        LeaveBalance balance = LeaveBalance.builder()
                .allocated(10)
                .used(8) // Only 2 days remaining
                .build();

        int currentYear = LocalDate.now().getYear();

        when(leaveBalanceRepository.findByEmployeeAndLeaveTypeAndYear(
                any(), any(), eq(currentYear)))
                .thenReturn(Optional.of(balance));

        // Act & Assert
        // Requesting 5 days (Jan 1 to Jan 5) when only 2 are left
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            leaveService.submitRequest(employee, LeaveRequest.LeaveType.ANNUAL_LEAVE,
                    LocalDate.of(currentYear, 1, 1),
                    LocalDate.of(currentYear, 1, 5),
                    "Vacation", null);
        });

        assertTrue(exception.getMessage().contains("Insufficient leave balance"));
    }
}