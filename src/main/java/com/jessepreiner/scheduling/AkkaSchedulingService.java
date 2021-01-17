package com.jessepreiner.scheduling;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.persistence.typed.PersistenceId;
import com.jessepreiner.scheduling.schedule.ScheduleActor;
import com.jessepreiner.scheduling.schedule.ScheduleData;
import com.jessepreiner.scheduling.schedule.ScheduleTags;
import com.jessepreiner.scheduling.schedule.protocol.commands.AddScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.commands.Command;
import com.jessepreiner.scheduling.schedule.protocol.commands.RetrieveScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.events.ScheduleAddedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import static com.jessepreiner.scheduling.schedule.ScheduleActor.ENTITY_TYPE_KEY;

public class AkkaSchedulingService implements SchedulingService {
    private static final Duration ASK_TIMEOUT = Duration.ofSeconds(5);
    private static final Logger LOGGER = LoggerFactory.getLogger(AkkaSchedulingService.class);
    private final ClusterSharding sharding;

    public AkkaSchedulingService(ActorSystem<Command> guardian) {
        sharding = ClusterSharding.get(guardian);

        sharding.init(
                Entity.of(
                        ENTITY_TYPE_KEY,
                        entityContext -> {
                            int tagIndex = Math.abs(entityContext.getEntityId().hashCode() % ScheduleTags.TAGS.length);
                            return ScheduleActor.create(PersistenceId.of(
                                    entityContext.getEntityTypeKey().name(),
                                    entityContext.getEntityId()),
                                    ScheduleTags.TAGS[tagIndex]
                            );
                        }));
    }

    @Override
    public CompletionStage<ScheduleServiceResult> addSchedule(LocalDateTime startTime, LocalDateTime endTime) {
        String scheduleId = UUID.randomUUID().toString();
        EntityRef<Command> entityRef =
                sharding.entityRefFor(ScheduleActor.ENTITY_TYPE_KEY, scheduleId);

        return entityRef
                .ask(replyTo -> new AddScheduleCommand(scheduleId, startTime, endTime, replyTo), ASK_TIMEOUT)
                .exceptionally(x -> new FailureScheduleServiceResult(x.getMessage()))
                .thenApply((reply) -> {
                    if (reply instanceof ScheduleAddedEvent) {
                        LOGGER.info("Schedule added " + reply.toString());
                        return new SuccessfulScheduleServiceResult(reply);
                    } else {
                        return new FailureScheduleServiceResult("Unknown reply received " + reply.getClass());
                    }
                });
    }

    @Override
    public CompletionStage<ScheduleServiceResult> getSchedule(String scheduleId) {

        return sharding.entityRefFor(ScheduleActor.ENTITY_TYPE_KEY, scheduleId).ask(
                replyTo -> new RetrieveScheduleCommand(scheduleId, replyTo),
                ASK_TIMEOUT)
                       .exceptionally(this::getThrowableObjectFunction)
                       .thenApply((reply) -> {
                           if (reply instanceof ScheduleData) {
                               ScheduleData d = (ScheduleData) reply;
                               LOGGER.info("Retrieved schedule " + reply.toString());
                               return new SuccessfulScheduleServiceResult(d);
                           } else {
                               throw new RuntimeException("Unknown type of reply: " + reply.getClass());
                           }
                       });
    }

    private FailureScheduleServiceResult getThrowableObjectFunction(Throwable t) {
        LOGGER.info("Got failure " + t.getMessage());
        return new FailureScheduleServiceResult(t.getMessage());
    }
}
