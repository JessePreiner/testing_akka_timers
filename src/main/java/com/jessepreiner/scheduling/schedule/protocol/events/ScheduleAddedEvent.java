package com.jessepreiner.scheduling.schedule.protocol.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.jessepreiner.scheduling.schedule.ScheduleData;

import java.util.Objects;

public class ScheduleAddedEvent implements Event {

    private final ScheduleData scheduleData;

    @JsonCreator
    public ScheduleAddedEvent(ScheduleData scheduleData) {
        this.scheduleData = scheduleData;
    }

    public ScheduleData getScheduleData() {
        return scheduleData;
    }

    @Override
    public String toString() {
        return "ScheduleAddedEvent{" +
            "scheduleData=" + scheduleData +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ScheduleAddedEvent that = (ScheduleAddedEvent) o;
        return Objects.equals(scheduleData, that.scheduleData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleData);
    }

}
