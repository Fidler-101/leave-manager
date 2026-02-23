package com.leavemanager.service;

import com.leavemanager.model.*;
import com.leavemanager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final PasswordEncoder passwordEncoder;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
    }

    public List<User> getAllEmployees() {
        return userRepository.findByRole(User.Role.EMPLOYEE);
    }

    public List<User> getAllManagers() {
        return userRepository.findByRole(User.Role.MANAGER);
    }

    @Transactional
    public User createUser(String firstName, String lastName, String email,
                            String rawPassword, String department, User.Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use: " + email);
        }
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .department(department)
                .role(role)
                .build();
        user = userRepository.save(user);
        initializeLeaveBalances(user);
        return user;
    }

    private void initializeLeaveBalances(User user) {
        int year = LocalDate.now().getYear();
        LeaveRequest.LeaveType[] types = {
                LeaveRequest.LeaveType.ANNUAL_LEAVE,
                LeaveRequest.LeaveType.SICK_LEAVE,
                LeaveRequest.LeaveType.PARENTAL_LEAVE
        };
        int[] allocations = { 20, 10, 30 };

        for (int i = 0; i < types.length; i++) {
            LeaveBalance balance = LeaveBalance.builder()
                    .employee(user)
                    .leaveType(types[i])
                    .year(year)
                    .allocated(allocations[i])
                    .used(0)
                    .build();
            leaveBalanceRepository.save(balance);
        }
    }
}
