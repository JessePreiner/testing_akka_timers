package com.jessepreiner.scheduling.schedule;

import com.jessepreiner.scheduling.schedule.protocol.JsonSerializable;

import java.time.LocalDateTime;

public class ScheduleData implements JsonSerializable {
    private final String scheduleId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private ScheduleStatus status;

    private ScheduleData(String scheduleId, LocalDateTime startTime, LocalDateTime endTime, ScheduleStatus status) {
        this.scheduleId = scheduleId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    static ScheduleData newSchedule(String scheduleId, LocalDateTime startTime, LocalDateTime endTime) {
        return new ScheduleData(scheduleId, startTime, endTime, ScheduleStatus.Pending);
    }

    void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    String getScheduleId() {
        return scheduleId;
    }

    LocalDateTime getStartTime() {
        return startTime;
    }

    LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "ScheduleData{" +
            "scheduleId='" + scheduleId + '\'' +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", status=" + status +
            '}';
    }

    ScheduleStatus getStatus() {
        return status;
    }

    enum ScheduleStatus { // todo model with FSM
        Pending, Active, Revoked, Expired
    }
}
