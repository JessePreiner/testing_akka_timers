package com.jessepreiner.scheduling;

import com.jessepreiner.scheduling.schedule.protocol.JsonSerializable;

import java.util.Objects;

public abstract class ScheduleServiceResult implements JsonSerializable {
    private final Object response;

    ScheduleServiceResult(Object response) {
        this.response = response;
    }

    public Object getResponse() {
        return response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleServiceResult that = (ScheduleServiceResult) o;
        return Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(response);
    }

    @Override
    public String toString() {
        return "ScheduleServiceResult{" +
                "response=" + response +
                '}';
    }
}
