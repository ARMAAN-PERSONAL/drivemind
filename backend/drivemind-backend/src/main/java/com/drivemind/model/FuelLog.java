package com.drivemind.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "fuel_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    // liters filled
    private Double liters;

    // total price paid
    private Double price;

    // odometer reading at fill-up
    private Integer mileage;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;
}