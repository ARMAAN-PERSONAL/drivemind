package com.drivemind.repository;

import com.drivemind.model.FuelLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FuelLogRepository extends JpaRepository<FuelLog, Long> {

    List<FuelLog> findByCarId(Long carId);

    long countByCarIdIn(List<Long> carIds);

    List<FuelLog> findByCarIdIn(List<Long> carIds);
}