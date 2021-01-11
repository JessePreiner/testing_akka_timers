package com.jessepreiner.scheduling.schedule.protocol.commands;

import akka.actor.typed.ActorRef;
import com.jessepreiner.scheduling.schedule.ScheduleData;

public class InitializeCommand implements Command {
    private final ScheduleData scheduleData;
    private final ActorRef<Object> replyTo;

    public InitializeCommand(ScheduleData scheduleData, ActorRef<Object> replyTo) {
        this.scheduleData = scheduleData;
        this.replyTo = replyTo;
    }

    @Override
    public String toString() {
        return "InitializeCommand{" +
                "scheduleData=" + scheduleData +
                ", replyTo=" + replyTo +
                '}';
    }

    public ScheduleData getScheduleData() {
        return scheduleData;
    }

    public ActorRef<Object> getReplyTo() {
        return replyTo;
    }
}
