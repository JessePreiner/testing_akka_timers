package com.jessepreiner.scheduling.schedule;

import akka.actor.typed.Behavior;
import akka.actor.typed.scaladsl.ActorContext;
import akka.actor.typed.scaladsl.Behaviors;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.CommandHandlerBuilder;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.persistence.typed.javadsl.Recovery;
import akka.persistence.typed.javadsl.ReplyEffect;
import com.jessepreiner.scheduling.schedule.protocol.commands.AddScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.commands.Command;
import com.jessepreiner.scheduling.schedule.protocol.commands.RetrieveScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.events.Event;
import com.jessepreiner.scheduling.schedule.protocol.events.ScheduleAddedEvent;
import com.jessepreiner.scheduling.schedule.protocol.state.State;

public class ScheduleActor extends EventSourcedBehavior<Command, Event, State> {

    public static final EntityTypeKey<Command> ENTITY_TYPE_KEY =
            EntityTypeKey.create(Command.class, "ScheduleActor");

    private final ActorContext<Command> context;

    private ScheduleActor(PersistenceId persistenceId, ActorContext<Command> ctx) {
        super(persistenceId);
        context = ctx;
        context.log().info("Creating actor " + persistenceId);
    }

    public static Behavior<Command> create(PersistenceId persistenceId) {
        return Behaviors.setup(context -> new ScheduleActor(persistenceId, context));
    }

    @Override
    public Recovery recovery() {
        context.log().info("Recovered");
        return super.recovery();
    }

    @Override
    public State emptyState() {
        return null;
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        CommandHandlerBuilder<Command, Event, State> commandEventStateCommandHandlerBuilder = newCommandHandlerBuilder();

        return commandEventStateCommandHandlerBuilder
                .forAnyState()
                .onCommand(AddScheduleCommand.class, this::handleAddSchedule)
                .onCommand(RetrieveScheduleCommand.class, this::handleRetrieveSchedule)
                .build();
    }

    private ReplyEffect<Event, State> handleAddSchedule(AddScheduleCommand addScheduleCommand) {
        ScheduleData scheduleData = ScheduleData.newSchedule(addScheduleCommand.getScheduleId(), addScheduleCommand.getStartTime(), addScheduleCommand.getEndTime());
        ScheduleAddedEvent scheduleAddedEvent = new ScheduleAddedEvent(scheduleData);
        return Effect().persist(scheduleAddedEvent).thenReply(
                addScheduleCommand.getReplyTo(), (state) -> scheduleAddedEvent);
    }

    private ReplyEffect<Event, State> handleRetrieveSchedule(State state, RetrieveScheduleCommand retrieveScheduleCommand) {
        return Effect().none().thenReply(
                retrieveScheduleCommand.getReplyTo(),
                (a) -> state.getScheduleData()
        );
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(ScheduleAddedEvent.class, this::handleScheduleAddedEvent)
                .build();
    }

    private State handleScheduleAddedEvent(ScheduleAddedEvent e) {
        return new State(e.getScheduleData());
    }
}