package com.leavemanager.controller;

import com.leavemanager.model.*;
import com.leavemanager.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final LeaveService leaveService;
    private final UserService userService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        List<LeaveRequest> pending = leaveService.getAllPendingRequests();
        List<User> employees = userService.getAllEmployees();

        model.addAttribute("user", user);
        model.addAttribute("pendingRequests", pending);
        model.addAttribute("pendingCount", pending.size());
        model.addAttribute("employeeCount", employees.size());
        model.addAttribute("onLeaveToday", getOnLeaveTodayCount());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("activePage", "dashboard");
        return "manager/dashboard";
    }

    @GetMapping("/approvals")
    public String approvals(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("pendingRequests", leaveService.getAllPendingRequests());
        model.addAttribute("activePage", "approvals");
        return "manager/approvals";
    }

    @PostMapping("/approve/{id}")
    public String approve(@AuthenticationPrincipal UserDetails userDetails,
                           @PathVariable Long id,
                           @RequestParam(required = false) String comment,
                           RedirectAttributes redirectAttrs) {
        User manager = getCurrentUser(userDetails);
        try {
            leaveService.approveRequest(id, manager, comment);
            redirectAttrs.addFlashAttribute("success", "Leave request approved successfully.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/dashboard";
    }

    @PostMapping("/reject/{id}")
    public String reject(@AuthenticationPrincipal UserDetails userDetails,
                          @PathVariable Long id,
                          @RequestParam(required = false) String comment,
                          RedirectAttributes redirectAttrs) {
        User manager = getCurrentUser(userDetails);
        try {
            leaveService.rejectRequest(id, manager, comment);
            redirectAttrs.addFlashAttribute("success", "Leave request rejected.");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/manager/dashboard";
    }

    @GetMapping("/history")
    public String history(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("allRequests", leaveService.getAllRequests());
        model.addAttribute("activePage", "history");
        return "manager/history";
    }

    @GetMapping("/team")
    public String team(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);
        model.addAttribute("employees", userService.getAllEmployees());
        model.addAttribute("activePage", "team");
        return "manager/team";
    }

    private long getOnLeaveTodayCount() {
        LocalDate today = LocalDate.now();
        return leaveService.getApprovedLeavesForMonth(today.getYear(), today.getMonthValue())
                .stream()
                .filter(r -> !today.isBefore(r.getStartDate()) && !today.isAfter(r.getEndDate()))
                .count();
    }
}
