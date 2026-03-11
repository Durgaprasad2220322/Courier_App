package com.courier.courierAppp.service;

import com.courier.courierAppp.entity.Delivery;
import com.courier.courierAppp.repository.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Add this import
import java.util.List;
import com.courier.courierAppp.entity.User;
@Service 
public class DeliveryService {

    @Autowired 
    private DeliveryRepository deliveryRepository;

    // Logic to create a new delivery
    public Delivery createDelivery(Delivery delivery) {
        // FIX: Only set to PENDING if the status is currently null (new delivery)
        if (delivery.getStatus() == null) {
            delivery.setStatus("PENDING");
        }
        return deliveryRepository.save(delivery); 
    }

    // New specific update method to ensure Oracle saves the change
    @Transactional
    public void updateStatus(Long id, String status) {
        Delivery delivery = deliveryRepository.findById(id).orElse(null);
        if (delivery != null) {
            delivery.setStatus(status);
            deliveryRepository.saveAndFlush(delivery); // Forces Oracle to update NOW
        }
    }
    
 // Inside DeliveryService.java
    public void acceptDelivery(Long deliveryId, User driver) {
        Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
        if (delivery != null) {
            delivery.setDriver(driver);
            delivery.setStatus("PICKED_UP");
            deliveryRepository.save(delivery);
        }
    }

    public void deleteDelivery(Long id) {
        deliveryRepository.deleteById(id);
    }

    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }
    
    public List<Delivery> searchDeliveries(String email) {
        return deliveryRepository.findBySenderEmailContainingIgnoreCase(email);
    }
    
    public Delivery getDeliveryById(Long id) {
        return deliveryRepository.findById(id).orElse(null);
    }
    public Delivery saveDelivery(Delivery delivery) {
        return deliveryRepository.save(delivery);
    }
}
