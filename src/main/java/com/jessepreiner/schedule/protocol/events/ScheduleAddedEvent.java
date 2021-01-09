package com.jessepreiner.schedule.protocol.events;

import java.time.LocalDateTime;

public class ScheduleAddedEvent implements Event {
    private final String scheduleId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    public ScheduleAddedEvent(String scheduleId, LocalDateTime startTime, LocalDateTime endTime) {
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
