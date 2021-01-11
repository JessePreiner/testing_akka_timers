package com.jessepreiner.scheduling;

import java.time.LocalDateTime;

interface SchedulingService {
    public ScheduleServiceResult addSchedule(String scheduleId, LocalDateTime startTime, LocalDateTime endTime);
    public ScheduleServiceResult getSchedule(String scheduleId);
}

abstract class ScheduleServiceResult {
}

class SuccessfulScheduleServiceResult extends ScheduleServiceResult {
}

class FailureScheduleServiceResult extends ScheduleServiceResult {
    private final String message;

    FailureScheduleServiceResult(String message) {
        this.message = message;
    }
}




