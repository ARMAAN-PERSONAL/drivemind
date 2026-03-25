package com.drivemind.repository;

import com.drivemind.model.Car;
import com.drivemind.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarRepository extends JpaRepository<Car, Long> {

    Optional<Car> findByVin(String vin);

    // 🔥 NEW: get cars by user
    List<Car> findByUser(User user);
}