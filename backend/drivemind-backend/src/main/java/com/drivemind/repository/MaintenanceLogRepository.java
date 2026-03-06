package com.drivemind.repository;

import com.drivemind.model.MaintenanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaintenanceLogRepository extends JpaRepository<MaintenanceLog, Long> {

    List<MaintenanceLog> findByCarId(Long carId);

}