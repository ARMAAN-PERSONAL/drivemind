package com.drivemind.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // VIN decoded vehicle identifier
    @Column(unique = true, nullable = false)
    private String vin;

    private String make;

    private String model;

    private Integer year;

    private String trim;

    // current odometer reading
    private Integer currentMileage;

    // 🔥 NEW: link car to user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // when vehicle was added to system
    private LocalDateTime createdAt;

    // relationship mappings

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<FuelLog> fuelLogs;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<MaintenanceLog> maintenanceLogs;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}