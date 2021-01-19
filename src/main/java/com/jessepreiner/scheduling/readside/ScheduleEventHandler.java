package com.jessepreiner.scheduling.readside;

import akka.projection.eventsourced.EventEnvelope;
import akka.projection.jdbc.javadsl.JdbcHandler;
import com.jessepreiner.scheduling.schedule.protocol.events.Event;
import com.jessepreiner.scheduling.schedule.protocol.events.ScheduleAddedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleEventHandler extends JdbcHandler<EventEnvelope<Event>, PlainJdbcSession> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleEventHandler.class);
    private final String tag;
    private final MySqlScheduleProjectionDao repo;

    ScheduleEventHandler(String tag) {
        this.tag = tag;
        repo = new MySqlScheduleProjectionDao();
    }


    @Override
    public void process(PlainJdbcSession session, EventEnvelope<Event> eventEventEnvelope) {
        LOGGER.info("Handling {} of type {}", eventEventEnvelope.toString(), eventEventEnvelope.event().getClass());

        if (eventEventEnvelope.event() instanceof ScheduleAddedEvent) {
            ScheduleAddedEvent scheduleAddedEvent = (ScheduleAddedEvent) eventEventEnvelope.event();
            LOGGER.info("Saving to schedule projection for event {}", scheduleAddedEvent);
            repo.save(new MySqlScheduleProjectionDao.ScheduleProjectionRecord(scheduleAddedEvent.getScheduleData().getScheduleId(), scheduleAddedEvent.getScheduleData().getStatus().name()), session);
        }


    }
}
