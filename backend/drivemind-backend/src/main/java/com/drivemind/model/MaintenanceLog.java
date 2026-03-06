package com.drivemind.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "maintenance_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // oil change, brakes, etc.

    private Double cost;

    private Integer mileage;

    private LocalDate serviceDate;

    @Column(length = 500)
    private String notes;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;
}