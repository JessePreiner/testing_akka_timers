package org.example;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;

import static org.example.ScheduleBehavior.*;

public class ScheduleBehavior extends AbstractBehavior<ScheduleCommand> {

    public interface ScheduleCommand {
    }
    public enum ProcessReminders implements ScheduleCommand {
        INSTANCE
    }
    private final Map<String, ScheduleData> schedules = new HashMap<>();

    public ScheduleBehavior(final TimerScheduler<ScheduleCommand> timer, final ActorContext<ScheduleCommand> context) {
        super(context);
        timer.startTimerWithFixedDelay("ReminderKey", ProcessReminders.INSTANCE, Duration.ofSeconds(15));

    }

    public static Behavior<ScheduleCommand> create() {
        return Behaviors.setup(context ->  Behaviors.withTimers(timers -> new ScheduleBehavior(timers, context)));
    }

    @Override
    public Receive<ScheduleCommand> createReceive() {
        return newReceiveBuilder()
            .onMessage(AddSchedule.class, this::handleAddSchedule)
            .onMessage(RetrieveSchedule.class, this::handleRetrieveSchedule)
            .onMessage(ProcessReminders.class, this::processReminders)
            .build();
    }

    private Behavior<ScheduleCommand> processReminders(ProcessReminders processReminders) {
        System.out.println("Pretending to process reminders, but really just setting all pending to active");
        List<ScheduleData> collect = this.schedules.values().stream().filter(x -> x.getStatus().equals(ScheduleData.ScheduleStatus.Pending)).collect(Collectors.toList());
        System.out.println("Found " + collect.size());
        collect.forEach(x -> x.setStatus(ScheduleData.ScheduleStatus.Active));
        return Behaviors.same();
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
