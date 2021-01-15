package com.jessepreiner.scheduling;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

public interface SchedulingService {
    CompletionStage<ScheduleServiceResult> addSchedule(LocalDateTime startTime, LocalDateTime endTime);
    CompletionStage<ScheduleServiceResult> getSchedule(String scheduleId);
}

class SuccessfulScheduleServiceResult<T> extends ScheduleServiceResult {
    private final T response;

    SuccessfulScheduleServiceResult(T response) {
        this.response = response;
    }

    T getResponse() {
        return this.response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SuccessfulScheduleServiceResult<?> that = (SuccessfulScheduleServiceResult<?>) o;
        return Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(response);
    }

    @Override
    public String toString() {
        return "SuccessfulScheduleServiceResult{" +
            "response=" + response +
            "} " + super.toString();
    }
}

class FailureScheduleServiceResult extends ScheduleServiceResult {
    private final String message;

    FailureScheduleServiceResult(String message) {
        this.message = message;
    }
}




