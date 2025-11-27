package com.example.monitor.dto;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private String sensorId;
    private LocalDateTime createdAt;

    public NotificationResponse() {}
}