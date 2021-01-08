package org.example;

import java.time.LocalDateTime;

public class ScheduleAddedEvent {
    private final String scheduleId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    ScheduleAddedEvent(String scheduleId, LocalDateTime startTime, LocalDateTime endTime) {
        this.scheduleId = scheduleId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "ScheduleAddedEvent{" +
            "scheduleId='" + scheduleId + '\'' +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            '}';
    }
}
