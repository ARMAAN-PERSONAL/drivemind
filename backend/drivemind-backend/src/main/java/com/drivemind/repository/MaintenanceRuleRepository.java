package com.drivemind.repository;

import com.drivemind.model.MaintenanceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaintenanceRuleRepository extends JpaRepository<MaintenanceRule, Long> {

    List<MaintenanceRule> findByCarId(Long carId);

    List<MaintenanceRule> findByCarIdOrderByServiceTypeAsc(Long carId);

    long countByCarIdIn(List<Long> carIds);
}