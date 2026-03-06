package com.drivemind.controller;

import com.drivemind.model.Notification;
import com.drivemind.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        return notificationService.createNotification(notification);
    }

    @GetMapping("/{carId}")
    public List<Notification> getNotifications(@PathVariable Long carId) {
        return notificationService.getNotificationsForCar(carId);
    }
}