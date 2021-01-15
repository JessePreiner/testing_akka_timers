package com.jessepreiner.scheduling.schedule;

import com.jessepreiner.scheduling.schedule.protocol.commands.Command;
import com.jessepreiner.scheduling.schedule.protocol.commands.InitializeCommand;
import com.jessepreiner.scheduling.schedule.protocol.commands.RetrieveScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.events.Event;
import com.jessepreiner.scheduling.schedule.protocol.events.ScheduleAddedEvent;
import com.jessepreiner.scheduling.schedule.protocol.state.State;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.actor.typed.scaladsl.ActorContext;
import akka.actor.typed.scaladsl.Behaviors;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandler;
import akka.persistence.typed.javadsl.CommandHandlerBuilder;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehavior;
import akka.persistence.typed.javadsl.Recovery;
import akka.persistence.typed.javadsl.ReplyEffect;

public class ScheduleActor extends EventSourcedBehavior<Command, Event, State> {

    public static final ServiceKey<Command> scheduleActorKey =
        ServiceKey.create(Command.class, "scheduleActor");
    private final ActorContext<Command> context;

    private ScheduleActor(PersistenceId persistenceId, ScheduleData scheduleData, ActorContext<Command> ctx, ActorRef<Object> creator) {
        super(persistenceId);
        context = ctx;
        System.out.println("Creating actor " + persistenceId);
        ctx.self().tell(new InitializeCommand(scheduleData, creator)); // cant remember how to do this in typed
    }

    static Behavior<Command> create(PersistenceId persistenceId, ScheduleData scheduleData, ActorRef<Object> creator) {
        return Behaviors.setup(ctx -> {
            ctx.system().receptionist().tell(Receptionist.register(scheduleActorKey, ctx.self()));
            return new ScheduleActor(persistenceId, scheduleData, ctx, creator);
        });
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
            .onCommand(InitializeCommand.class, this::initialize)
            .onCommand(RetrieveScheduleCommand.class, this::handleRetrieveSchedule)
            .build();
    }

    private ReplyEffect<Event, State> initialize(InitializeCommand initializeCommand) {
        ScheduleAddedEvent event = new ScheduleAddedEvent(initializeCommand.getScheduleData());
        return Effect().persist(event).thenReply(
            initializeCommand.getReplyTo(), (a) -> event);
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

    private State handleScheduleAddedEvent(State s, ScheduleAddedEvent e) {
        return new State(e.getScheduleData());
    }
}