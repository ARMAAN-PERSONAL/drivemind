package com.drivemind.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;

    private Boolean isRead;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;
}