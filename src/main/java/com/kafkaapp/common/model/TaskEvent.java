package com.kafkaapp.common.model;

import java.time.LocalDateTime;

/**
 * Model untuk event Task yang akan dikirim melalui Kafka
 */
public class TaskEvent {
    
    // Tipe event
    public enum EventType {
        CREATE, UPDATE, DELETE
    }
    
    private EventType eventType;
    private Task task;
    private LocalDateTime eventTime;
    
    // Default constructor untuk deserializasi
    public TaskEvent() {
        this.eventTime = LocalDateTime.now();
    }
    
    // Constructor dengan parameter
    public TaskEvent(EventType eventType, Task task) {
        this.eventType = eventType;
        this.task = task;
        this.eventTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public EventType getEventType() {
        return eventType;
    }
    
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
    
    public Task getTask() {
        return task;
    }
    
    public void setTask(Task task) {
        this.task = task;
    }
    
    public LocalDateTime getEventTime() {
        return eventTime;
    }
    
    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }
    
    @Override
    public String toString() {
        return "TaskEvent{" +
                "eventType=" + eventType +
                ", task=" + task +
                ", eventTime=" + eventTime +
                '}';
    }
} 