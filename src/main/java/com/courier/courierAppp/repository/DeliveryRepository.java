package com.courier.courierAppp.repository;

import com.courier.courierAppp.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    
	// This allows us to save deliveries and track their status
	List<Delivery> findBySenderEmailContainingIgnoreCase(String email);
}
