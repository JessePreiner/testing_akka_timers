package com.jessepreiner.scheduling.readside;

import akka.actor.typed.ActorSystem;
import akka.cluster.sharding.typed.ShardedDaemonProcessSettings;
import akka.cluster.sharding.typed.javadsl.ShardedDaemonProcess;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.Offset;
import akka.projection.ProjectionBehavior;
import akka.projection.ProjectionId;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.eventsourced.javadsl.EventSourcedProvider;
import akka.projection.javadsl.ExactlyOnceProjection;
import akka.projection.javadsl.SourceProvider;
import akka.projection.jdbc.javadsl.JdbcHandler;
import akka.projection.jdbc.javadsl.JdbcProjection;
import com.jessepreiner.scheduling.schedule.ScheduleTags;
import com.jessepreiner.scheduling.schedule.protocol.events.Event;

import java.util.Optional;

public class ScheduleProjection {
    public static void init(ActorSystem<?> system) {
        ShardedDaemonProcess.get(system)
                            .init(
                                    ProjectionBehavior.Command.class,
                                    "ScheduleEventsProjection",
                                    ScheduleTags.TAGS.length,
                                    index -> ProjectionBehavior.create(createProjectionFor(system, index)),
                                    ShardedDaemonProcessSettings.create(system),
                                    Optional.of(ProjectionBehavior.stopMessage()));
    }

    private static ExactlyOnceProjection<Offset, EventEnvelope<Event>> createProjectionFor(ActorSystem<?> system, int index) {
        String tag = ScheduleTags.TAGS[index];

        SourceProvider<Offset, EventEnvelope<Event>> sourceProvider =
                EventSourcedProvider.eventsByTag(
                        system,
                        CassandraReadJournal.Identifier(),
                        tag);

        return JdbcProjection.exactlyOnce(
                ProjectionId.of("ScheduleProjectionRecord", tag),
                sourceProvider,
                JdbcSessionFactory::newInstance,
                () -> new ScheduleEventHandler(tag),
                system);
    }


}
