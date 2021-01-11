package com.jessepreiner.scheduling.schedule.protocol.commands;

import akka.actor.typed.ActorRef;

public class RetrieveScheduleCommand implements Command {
    private final String scheduleId;
    private final ActorRef<Object> replyTo;

    public RetrieveScheduleCommand(String scheduleId, ActorRef<Object> replyTo) {
        this.scheduleId = scheduleId;
        this.replyTo = replyTo;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public ActorRef<Object> getReplyTo() {
        return replyTo;
    }

    @Override
    public String toString() {
        return "RetrieveScheduleCommand{" +
            "scheduleId='" + scheduleId + '\'' +
            ", replyTo=" + replyTo +
            '}';
    }
}
