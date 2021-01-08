package org.example;

import akka.actor.typed.ActorRef;

public class RetrieveSchedule implements ScheduleBehavior.ScheduleCommand {
    private final String scheduleId;
    private final ActorRef<Object> replyTo;

    RetrieveSchedule(String scheduleId, ActorRef<Object> replyTo) {
        this.scheduleId = scheduleId;
        this.replyTo = replyTo;
    }

    String getScheduleId() {
        return scheduleId;
    }

    ActorRef<Object> getReplyTo() {
        return replyTo;
    }

    @Override
    public String toString() {
        return "RetrieveSchedule{" +
            "scheduleId='" + scheduleId + '\'' +
            ", replyTo=" + replyTo +
            '}';
    }
}
