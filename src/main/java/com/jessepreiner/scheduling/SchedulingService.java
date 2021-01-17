package com.jessepreiner.scheduling;

import java.time.LocalDateTime;
import java.util.concurrent.CompletionStage;

public interface SchedulingService {
    CompletionStage<ScheduleServiceResult> addSchedule(LocalDateTime startTime, LocalDateTime endTime);
    CompletionStage<ScheduleServiceResult> getSchedule(String scheduleId);
}

class SuccessfulScheduleServiceResult extends ScheduleServiceResult {

    SuccessfulScheduleServiceResult(Object response) {
        super(response);
    }
}

class FailureScheduleServiceResult extends ScheduleServiceResult {
    private final String message;

    FailureScheduleServiceResult(String message) {
        super(null);
        this.message = message;
    }
}




