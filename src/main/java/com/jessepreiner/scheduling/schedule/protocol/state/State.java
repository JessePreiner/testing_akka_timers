package com.jessepreiner.scheduling.schedule.protocol.state;

import com.jessepreiner.scheduling.schedule.ScheduleData;
import com.jessepreiner.scheduling.schedule.protocol.JsonSerializable;

public class State implements JsonSerializable {
    private final ScheduleData scheduleData;

    public State(ScheduleData scheduleData) {
        this.scheduleData = scheduleData;
    }

    public State() {
        this.scheduleData = null;
    }

    public ScheduleData getScheduleData() {
        return this.scheduleData;
    }
}

// todo define types of state for FSM
