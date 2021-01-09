package org.example;

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

import static org.example.ScheduleBehavior.ScheduleCommand;

public class ScheduleBehavior extends AbstractBehavior<ScheduleCommand> {

    private ScheduleBehavior(final TimerScheduler<ScheduleCommand> timer, final ActorContext<ScheduleCommand> context) {
        super(context);
        timer.startTimerWithFixedDelay("ReminderKey", ProcessReminders.INSTANCE, Duration.ofSeconds(15));

    }

    public enum ProcessReminders implements ScheduleCommand {
        INSTANCE
    }

    private final Map<String, ScheduleData> schedules = new HashMap<>();

    static Behavior<ScheduleCommand> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new ScheduleBehavior(timers, context)));
    }

    @Override
    public Receive<ScheduleCommand> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddSchedule.class, this::handleAddSchedule)
                .onMessage(RetrieveSchedule.class, this::handleRetrieveSchedule)
                .onMessage(ProcessReminders.class, this::processReminders)
                .build();
    }

    @SuppressWarnings("unused")
    private Behavior<ScheduleCommand> processReminders(ProcessReminders processReminders) {
        this.schedules.values().stream()
                      .filter(x -> x.getStatus().equals(ScheduleData.ScheduleStatus.Pending))
                      .forEach(x -> x.setStatus(ScheduleData.ScheduleStatus.Active));
        return Behaviors.same();
    }

    interface ScheduleCommand {
    }

    private Behavior<ScheduleCommand> handleRetrieveSchedule(RetrieveSchedule retrieveSchedule) {
        retrieveSchedule.getReplyTo().tell(schedules.get(retrieveSchedule.getScheduleId()));
        return Behaviors.same();
    }

    private Behavior<ScheduleCommand> handleAddSchedule(AddSchedule scheduleCommand) {
        String scheduleId = UUID.randomUUID().toString();
        this.schedules.put(scheduleId, ScheduleData.newSchedule(scheduleId, scheduleCommand.getStartTime(), scheduleCommand.getEndTime()));
        scheduleCommand.getReplyTo().tell(new ScheduleAddedEvent(scheduleId, scheduleCommand.getStartTime(), scheduleCommand.getEndTime()));
        return Behaviors.same();
    }

}
