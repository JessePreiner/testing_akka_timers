package com.jessepreiner.scheduling.schedule;

import akka.actor.typed.Behavior;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.actor.typed.scaladsl.ActorContext;
import akka.actor.typed.scaladsl.Behaviors;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.CommandHandlerBuilder;
import akka.persistence.typed.javadsl.Effect;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import com.jessepreiner.scheduling.schedule.protocol.commands.Command;
import com.jessepreiner.scheduling.schedule.protocol.commands.InitializeCommand;
import com.jessepreiner.scheduling.schedule.protocol.commands.RetrieveScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.events.Event;
import com.jessepreiner.scheduling.schedule.protocol.events.ScheduleAddedEvent;
import com.jessepreiner.scheduling.schedule.protocol.state.State;

public class ScheduleActor extends EventSourcedBehavior<Command, Event, State> {

    public static final ServiceKey<Command> scheduleActorKey =
            ServiceKey.create(Command.class, "scheduleActor");

    private ScheduleActor(PersistenceId persistenceId, ScheduleData scheduleData, ActorContext<Command> ctx) {
        super(persistenceId);
        System.out.println("Creating actor " + persistenceId);
        ctx.self().tell(new InitializeCommand(scheduleData, null)); // cant remember how to do this in typed
    }

    static Behavior<Command> create(PersistenceId persistenceId, ScheduleData scheduleData) {
        return Behaviors.setup(ctx -> {
            ctx.system().receptionist().tell(Receptionist.register(scheduleActorKey, ctx.self()));
            return new ScheduleActor(persistenceId, scheduleData, ctx);
        });
    }

    @Override
    public State emptyState() {
        return null;
    }

    @Override
    public CommandHandler<Command, Event, State> commandHandler() {
        CommandHandlerBuilder<Command, Event, State> commandEventStateCommandHandlerBuilder = newCommandHandlerBuilder();

        commandEventStateCommandHandlerBuilder
                .forNullState()
                .onCommand(InitializeCommand.class, this::initialize);
        return commandEventStateCommandHandlerBuilder
                .forAnyState()
                .onCommand(RetrieveScheduleCommand.class, this::handleRetrieveSchedule)
                .build();
    }

    private Effect<Event, State> initialize(InitializeCommand initializeCommand) {
        return Effect().persist(new ScheduleAddedEvent(initializeCommand.getScheduleData())).thenReply(
                initializeCommand.getReplyTo(), (a) -> "OK"); // TODO revisit responses
    }

    private Effect<Event, State> handleRetrieveSchedule(State state, RetrieveScheduleCommand retrieveScheduleCommand) {
        retrieveScheduleCommand.getReplyTo().tell(state.getScheduleData());
        return Effect().none();
    }

    @Override
    public EventHandler<State, Event> eventHandler() {
        return newEventHandlerBuilder()
                .build();
    }

//    private Behavior<Command> handleRetrieveSchedule(RetrieveScheduleCommand retrieveScheduleCommand) {
//        retrieveScheduleCommand.getReplyTo().tell(schedules.get(retrieveScheduleCommand.getScheduleId()));
//        return Behaviors.same();
//    }
}