package com.courier.courierAppp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "deliveries") // This creates a table named 'deliveries' in your database
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String pickupAddress;
    private String deliveryAddress;
    @Column(name="status")
    private String status; // We will use simple text like "PENDING", "PICKED_UP", or "DELIVERED"

    // This links the delivery to the User who is sending the package
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    // This links the delivery to the User who is the driver
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private User driver;
    
    private Double weight;
    
    private String receiverName;
    private String receiverPhone;

    // 1. Default Constructor (Required for Spring Boot/JPA)
    public Delivery() {
    }

    // 2. Getters and Setters (The "Remote Controls" for your data)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    // Inside your class
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime orderDate = LocalDateTime.now(); // Sets current time by default

    // Add Getter and Setter
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
}
