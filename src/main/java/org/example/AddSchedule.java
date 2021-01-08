package org.example;

import java.time.LocalDateTime;

import akka.actor.typed.ActorRef;

class AddSchedule implements ScheduleBehavior.ScheduleCommand {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final ActorRef<Object> replyTo;

    AddSchedule(LocalDateTime startTime, LocalDateTime endTime, ActorRef<Object> replyTo) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.replyTo = replyTo;
    }

    LocalDateTime getStartTime() {
        return startTime;
    }

    LocalDateTime getEndTime() {
        return endTime;
    }

    ActorRef<Object> getReplyTo() {
        return replyTo;
    }
}
