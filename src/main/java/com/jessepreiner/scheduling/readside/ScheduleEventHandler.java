package com.jessepreiner.scheduling.readside;

import akka.Done;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.javadsl.Handler;
import com.jessepreiner.App;
import com.jessepreiner.scheduling.schedule.protocol.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ScheduleEventHandler extends Handler<EventEnvelope<Event>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleEventHandler.class);
    private final String tag;

    public ScheduleEventHandler(String tag) {
        this.tag = tag;
    }

    @Override
    public CompletionStage<Done> process(EventEnvelope<Event> eventEventEnvelope) {
        LOGGER.info("Handling {} of type {}",  eventEventEnvelope.toString(), eventEventEnvelope.event().getClass());
        return CompletableFuture.completedFuture(Done.getInstance());
    }
}
