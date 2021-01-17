package com.jessepreiner.scheduling.schedule.protocol.commands;

import java.time.LocalDateTime;

import akka.actor.typed.ActorRef;

public class AddScheduleCommand implements Command {
    private final String scheduleId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final ActorRef<Object> replyTo;

    public AddScheduleCommand(String scheduleId, LocalDateTime startTime, LocalDateTime endTime, ActorRef<Object> replyTo) {
        this.scheduleId = scheduleId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.replyTo = replyTo;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public ActorRef<Object> getReplyTo() {
        return replyTo;
    }

    public String getScheduleId() {
        return scheduleId;
    }
}
