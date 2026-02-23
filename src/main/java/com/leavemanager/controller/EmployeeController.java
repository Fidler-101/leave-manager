package com.leavemanager.controller;

import com.leavemanager.model.*;
import com.leavemanager.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final LeaveService leaveService;
    private final UserService userService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        List<LeaveBalance> balances = leaveService.getMyBalances(user);
        List<LeaveRequest> requests = leaveService.getMyRequests(user);

        model.addAttribute("user", user);
        model.addAttribute("balances", balances);
        model.addAttribute("recentRequests", requests.stream().limit(5).toList());
        model.addAttribute("leaveTypes", LeaveRequest.LeaveType.values());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activePage", "dashboard");
        return "employee/dashboard";
    }

    @GetMapping("/apply")
    public String applyPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("leaveTypes", LeaveRequest.LeaveType.values());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activePage", "apply");
        return "employee/apply";
    }

    @PostMapping("/apply")
    public String submitLeave(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String leaveType,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                               @RequestParam(required = false) String reason,
                               RedirectAttributes redirectAttrs) {
        User user = getCurrentUser(userDetails);
        try {
            leaveService.submitRequest(user,
                    LeaveRequest.LeaveType.valueOf(leaveType),
                    startDate, endDate, reason, null);
            redirectAttrs.addFlashAttribute("success", "Leave request submitted successfully!");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/dashboard";
    }

    @PostMapping("/cancel/{id}")
    public String cancelLeave(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id,
                               RedirectAttributes redirectAttrs) {
        User user = getCurrentUser(userDetails);
        try {
            leaveService.cancelRequest(id, user);
            redirectAttrs.addFlashAttribute("success", "Leave request cancelled.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/employee/dashboard";
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("requests", leaveService.getMyRequests(user));
        model.addAttribute("activePage", "history");
        return "employee/history";
    }
}
