package com.jessepreiner.scheduling;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import com.jessepreiner.scheduling.schedule.ScheduleData;
import com.jessepreiner.scheduling.schedule.ScheduleSupervisor;
import com.jessepreiner.scheduling.schedule.protocol.commands.AddScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.commands.Command;
import com.jessepreiner.scheduling.schedule.protocol.commands.RetrieveScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.events.ScheduleAddedEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletionStage;

public class AkkaSchedulingService implements SchedulingService {
    private static final Duration ASK_TIMEOUT = Duration.ofSeconds(5);
    private ActorSystem<Command> guardian = ActorSystem.create(ScheduleSupervisor.create(), "Empty");

    @Override
    public CompletionStage<ScheduleServiceResult> addSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        CompletionStage<Object> ask = AskPattern.ask(
                guardian,
                replyTo -> new AddScheduleCommand(startTime, endTime, replyTo),
                ASK_TIMEOUT,
                guardian.scheduler()
        );
        return ask.exceptionally(x -> new FailureScheduleServiceResult(x.getMessage()))
                  .thenApply((reply) -> {
                      if (reply instanceof ScheduleAddedEvent) {
                          System.out.println("Schedule added " + reply.toString());
                          return new SuccessfulScheduleServiceResult(reply);
                      } else {
                          throw new RuntimeException("Unknown type of reply: " + reply.getClass());
                      }
                  });
    }

    @Override
    public CompletionStage<ScheduleServiceResult> getSchedule(String scheduleId) {

        return AskPattern.ask(
                        guardian,
                        replyTo -> new RetrieveScheduleCommand(scheduleId, replyTo),
                        ASK_TIMEOUT,
                        guardian.scheduler())
                .exceptionally(this::getThrowableObjectFunction)
                          .thenApply((reply) -> {
                              if (reply instanceof ScheduleData) {
                                  ScheduleData d = (ScheduleData)reply;
                                  System.out.println("Retrieved schedule " + reply.toString());
                                  return new SuccessfulScheduleServiceResult(d);
                              } else {
                                  throw new RuntimeException("Unknown type of reply: " + reply.getClass());
                              }
                          });
    }

    private FailureScheduleServiceResult getThrowableObjectFunction(Throwable t) {
            System.out.println("Got failure " + t.getMessage());
            return new FailureScheduleServiceResult(t.getMessage());
    }
}
