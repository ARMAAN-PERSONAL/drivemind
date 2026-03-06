package com.drivemind.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "maintenance_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceType;

    // distance interval for service
    private Integer intervalKm;

    private String description;
}