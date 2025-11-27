package com.example.monitor.dto;
import lombok.Data;
@Data
public class NotificationRequest {
    private String title;
    private String message;
    private String type;
    private String sensorId;

    public NotificationRequest() {}

    public NotificationRequest(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
    }
}