package com.jessepreiner.schedule;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jessepreiner.schedule.protocol.commands.AddScheduleCommand;
import com.jessepreiner.schedule.protocol.commands.Command;
import com.jessepreiner.schedule.protocol.commands.ProcessRemindersCommand;
import com.jessepreiner.schedule.protocol.commands.RetrieveScheduleCommand;
import com.jessepreiner.schedule.protocol.events.ScheduleAddedEvent;

public class ScheduleBehavior extends AbstractBehavior<Command> {

    private ScheduleBehavior(final TimerScheduler<Command> timer, final ActorContext<Command> context) {
        super(context);
        timer.startTimerWithFixedDelay("ReminderKey", ProcessRemindersCommand.INSTANCE, Duration.ofSeconds(15));
    }

    private final Map<String, ScheduleData> schedules = new HashMap<>();

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new ScheduleBehavior(timers, context)));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddScheduleCommand.class, this::handleAddSchedule)
                .onMessage(RetrieveScheduleCommand.class, this::handleRetrieveSchedule)
                .onMessage(ProcessRemindersCommand.class, this::processReminders)
                .build();
    }

    @SuppressWarnings("unused")
    private Behavior<Command> processReminders(ProcessRemindersCommand processRemindersCommand) {
        this.schedules.values().stream()
                      .filter(scheduleData -> scheduleData.getStatus().equals(ScheduleData.ScheduleStatus.Pending))
                      .forEach(scheduleData -> scheduleData.setStatus(ScheduleData.ScheduleStatus.Active));
        return Behaviors.same();
    }

    private Behavior<Command> handleRetrieveSchedule(RetrieveScheduleCommand retrieveScheduleCommand) {
        retrieveScheduleCommand.getReplyTo().tell(schedules.get(retrieveScheduleCommand.getScheduleId()));
        return Behaviors.same();
    }

    private Behavior<Command> handleAddSchedule(AddScheduleCommand addSchedule) {
        // todo when this becomes a supervisor, this will create a schedule actor
        String scheduleId = UUID.randomUUID().toString();
        this.schedules.put(scheduleId, ScheduleData.newSchedule(scheduleId, addSchedule.getStartTime(), addSchedule.getEndTime()));
        addSchedule.getReplyTo().tell(new ScheduleAddedEvent(scheduleId, addSchedule.getStartTime(), addSchedule.getEndTime()));
        return Behaviors.same();
    }

}
