package com.jessepreiner.scheduling.schedule;

import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.TimerScheduler;
import akka.persistence.typed.PersistenceId;
import com.jessepreiner.scheduling.schedule.protocol.commands.AddScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.commands.Command;
import com.jessepreiner.scheduling.schedule.protocol.commands.ProcessRemindersCommand;
import com.jessepreiner.scheduling.schedule.protocol.events.ScheduleAddedEvent;

import java.time.Duration;
import java.util.UUID;

public class ScheduleSupervisor extends AbstractBehavior<Command> {

    private ScheduleSupervisor(final TimerScheduler<Command> timer, final ActorContext<Command> context) {
        super(context);
        // todo move this to diff actor
        //timer.startTimerWithFixedDelay("ReminderKey", ProcessRemindersCommand.INSTANCE, Duration.ofSeconds(15));
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(context -> Behaviors.withTimers(timers -> new ScheduleSupervisor(timers, context)));
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddScheduleCommand.class, this::handleCreateSchedule)
                .build();
    }


    private Behavior<Command> handleCreateSchedule(AddScheduleCommand addSchedule) {
        String scheduleId = UUID.randomUUID().toString();
        ScheduleData scheduleData = ScheduleData.newSchedule(scheduleId, addSchedule.getStartTime(), addSchedule.getEndTime());
        Behavior<Command> behavior = ScheduleActor.create(PersistenceId.ofUniqueId(scheduleId), scheduleData, addSchedule.getReplyTo());

        getContext().spawn(
                Behaviors.supervise(behavior).onFailure(SupervisorStrategy.restart().withLimit(10, Duration.ofMinutes(1))),
                "schedule-" + scheduleId);

        return Behaviors.same();
    }
}
