package com.courier.courierAppp.controller;

import com.courier.courierAppp.entity.Delivery;
import com.courier.courierAppp.entity.User;
import com.courier.courierAppp.service.DeliveryService;
import com.courier.courierAppp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private UserRepository userRepository;

    // 1. Show Form
    @GetMapping("/create")
    public String showCreateDeliveryPage(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/api/users/login";

        List<User> allUsers = userRepository.findAll(); 
        List<User> drivers = allUsers.stream()
                .filter(u -> "DRIVER".equalsIgnoreCase(u.getRole()))
                .filter(new java.util.function.Predicate<User>() {
                    private java.util.Set<String> names = new java.util.HashSet<>();
                    @Override
                    public boolean test(User u) {
                        return names.add(u.getName()); // Only adds if name is unique
                    }
                })
                .toList();

        model.addAttribute("delivery", new Delivery());
        model.addAttribute("users", allUsers); 
        model.addAttribute("drivers", drivers);

        return "create-delivery";
    }
    
    // 2. Process Form (Post-Redirect-Get Pattern)
    @PostMapping("/create")
    public String createDelivery(@ModelAttribute Delivery delivery, HttpSession session, RedirectAttributes redirectAttributes) {
        delivery.setStatus("PENDING");
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (delivery.getSender() == null) {
            delivery.setSender(loggedInUser);
        }
        
        // Save to Oracle
        Delivery savedDelivery = deliveryService.saveDelivery(delivery);
        
        // Calculate Price
        int price = (delivery.getWeight() != null && delivery.getWeight() > 10) ? 150 : 50;

        // Use Flash Attributes to pass data safely to the redirect page
        redirectAttributes.addFlashAttribute("delivery", savedDelivery);
        redirectAttributes.addFlashAttribute("calculatedPrice", price);

        return "redirect:/api/deliveries/confirm";
    }

    // 3. New GET mapping for the Confirmation (Payment) Page
    @GetMapping("/confirm")
    public String showConfirmPage() {
        return "payment";
    }

    // 4. Dashboard with Filters
    @GetMapping("/dashboard")
    public String viewDashboard(@RequestParam(value = "search", required = false) String search, 
                                HttpSession session, 
                                Model model) {
        
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) return "redirect:/api/users/login";

        List<Delivery> allDeliveries = deliveryService.getAllDeliveries();
        List<Delivery> displayList;

        if ("DRIVER".equalsIgnoreCase(loggedInUser.getRole())) {
            // 1. Available Jobs (Jobs no one has taken yet)
            displayList = allDeliveries.stream()
                .filter(d -> "PENDING".equalsIgnoreCase(d.getStatus()))
                .toList();

            // 2. My Tasks (Jobs THIS specific driver has accepted)
            List<Delivery> myTasks = allDeliveries.stream()
                .filter(d -> d.getDriver() != null && d.getDriver().getId().equals(loggedInUser.getId()))
                .filter(d -> "PICKED_UP".equalsIgnoreCase(d.getStatus()))
                .toList();
            
            model.addAttribute("myTasks", myTasks); // Send this to the new table in HTML
        } 
        else if ("ADMIN".equalsIgnoreCase(loggedInUser.getRole())) {
            displayList = allDeliveries; // Admins see everything
        } 
        else {
            // Customers see only their own deliveries
            displayList = allDeliveries.stream()
                .filter(d -> d.getSender() != null && d.getSender().getId().equals(loggedInUser.getId()))
                .toList();
        }

        // Apply Search to the main list
        if (search != null && !search.isEmpty()) {
            displayList = displayList.stream()
                .filter(d -> d.getPickupAddress().toLowerCase().contains(search.toLowerCase()) || 
                             (d.getSender() != null && d.getSender().getEmail().toLowerCase().contains(search.toLowerCase())))
                .toList();
        }
        
     // Calculate Total Earnings: Sum of 'price' for all 'DELIVERED' tasks for this driver
        int totalEarnings = allDeliveries.stream()
            .filter(d -> d.getDriver() != null && d.getDriver().getId().equals(loggedInUser.getId()))
            .filter(d -> "DELIVERED".equalsIgnoreCase(d.getStatus()))
            .mapToInt(d -> (d.getWeight() != null && d.getWeight() > 10) ? 150 : 50)
            .sum();
        
        model.addAttribute("deliveries", displayList);
        model.addAttribute("totalCount", displayList.size());
        model.addAttribute("pendingCount", allDeliveries.stream().filter(d -> "PENDING".equalsIgnoreCase(d.getStatus())).count());
        model.addAttribute("deliveredCount", allDeliveries.stream().filter(d -> "DELIVERED".equalsIgnoreCase(d.getStatus())).count());
        model.addAttribute("totalEarnings", totalEarnings);
        return "dashboard";
    }

    @GetMapping("/status/{id}/{status}")
    public String updateStatus(@PathVariable Long id, @PathVariable String status) {
        deliveryService.updateStatus(id, status); 
        return "redirect:/api/deliveries/dashboard";
    }

    @GetMapping("/delete/{id}")
    public String deleteDelivery(@PathVariable Long id) {
        deliveryService.deleteDelivery(id);
        return "redirect:/api/deliveries/dashboard";
    }
    
    @GetMapping("/details/{id}")
    public String viewDeliveryDetails(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/api/users/login";
        
        Delivery delivery = deliveryService.getDeliveryById(id);
        model.addAttribute("delivery", delivery);
        return "delivery-details";
    }
    
 // Inside DeliveryController.java
    @GetMapping("/accept/{id}")
    public String acceptDelivery(@PathVariable Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        // Safety check: Only drivers should be able to accept
        if (loggedInUser != null && "DRIVER".equalsIgnoreCase(loggedInUser.getRole())) {
            deliveryService.acceptDelivery(id, loggedInUser);
        }
        
        return "redirect:/api/deliveries/dashboard";
    }
    
    @GetMapping("/track/{id}")
    public String trackDelivery(@PathVariable Long id, Model model) {
        Delivery delivery = deliveryService.getDeliveryById(id);
        model.addAttribute("delivery", delivery);
        
        // Calculate progress percentage for the CSS bar
        int progress = 0;
        if ("PENDING".equalsIgnoreCase(delivery.getStatus())) progress = 33;
        else if ("PICKED_UP".equalsIgnoreCase(delivery.getStatus())) progress = 66;
        else if ("DELIVERED".equalsIgnoreCase(delivery.getStatus())) progress = 100;
        
        model.addAttribute("progress", progress);
        return "track-delivery";
    }
}
