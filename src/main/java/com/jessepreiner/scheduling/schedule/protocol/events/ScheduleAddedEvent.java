package com.jessepreiner.scheduling.schedule.protocol.events;

import com.jessepreiner.scheduling.schedule.ScheduleData;

public class ScheduleAddedEvent implements Event {

    private final ScheduleData scheduleData;

    public ScheduleAddedEvent(ScheduleData scheduleData) {
        this.scheduleData = scheduleData;
    }

    @Override
    public String toString() {
        return "ScheduleAddedEvent{" +
                "scheduleData=" + scheduleData +
                '}';
    }
}
