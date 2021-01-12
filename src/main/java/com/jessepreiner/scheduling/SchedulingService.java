package com.jessepreiner.scheduling;

import java.time.LocalDateTime;
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
}

class FailureScheduleServiceResult extends ScheduleServiceResult {
    private final String message;

    FailureScheduleServiceResult(String message) {
        this.message = message;
    }
}




