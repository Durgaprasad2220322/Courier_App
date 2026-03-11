package com.courier.courierAppp.service;

import com.courier.courierAppp.entity.User;
import com.courier.courierAppp.repository.UserRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {
        // Here you could add logic to check if the email already exists
        return userRepository.save(user);
    }
    

    public User login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password))
                .orElse(null);
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public void saveUser(User user) {
        userRepository.save(user);
    }
}
