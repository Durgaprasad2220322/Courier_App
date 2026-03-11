package com.courier.courierAppp.controller;

import com.courier.courierAppp.entity.User;
import com.courier.courierAppp.service.UserService;
import jakarta.servlet.http.HttpSession; // Add this import

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Add this import
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userService.registerUser(user);
        return "redirect:/api/users/login"; 
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam String email, 
                            @RequestParam String password, 
                            HttpSession session, 
                            Model model) {
        
        User user = userService.login(email, password);
        
        if (user != null) {
            // 1. Store the user in the session
            session.setAttribute("loggedInUser", user);
            return "redirect:/api/deliveries/dashboard"; 
        } else {
            // 2. Pass an error message to the login page
            model.addAttribute("error", "Invalid email or password!");
            return "login"; 
        }
    }

    // Add a Logout method too!
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/api/users/login";
    }
    @GetMapping("/manage")
    public String manageUsers(@RequestParam(value = "keyword", required = false) String keyword, 
                              Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !"ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            return "redirect:/api/users/login";
        }

        List<User> users;
        if (keyword != null && !keyword.isEmpty()) {
            // Simple search logic using streams
            users = userService.getAllUsers().stream()
                    .filter(u -> u.getName().toLowerCase().contains(keyword.toLowerCase()) || 
                                 u.getEmail().toLowerCase().contains(keyword.toLowerCase()))
                    .toList();
        } else {
            users = userService.getAllUsers();
        }

        model.addAttribute("users", users);
        return "manage-users";
    }
    
    @GetMapping("/toggle-status")
    public String toggleStatus(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user != null) {
            user.setOnline(!user.isOnline());
            userService.saveUser(user); // Save the new status to Oracle
        }
        return "redirect:/api/deliveries/dashboard";
    }
}
