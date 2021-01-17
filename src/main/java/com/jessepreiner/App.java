package com.jessepreiner;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.projection.eventsourced.EventEnvelope;
import akka.projection.javadsl.Handler;
import akka.stream.alpakka.cassandra.javadsl.CassandraSession;
import akka.stream.alpakka.cassandra.javadsl.CassandraSessionRegistry;
import com.jessepreiner.scheduling.AkkaSchedulingService;
import com.jessepreiner.scheduling.readside.ScheduleProjection;
import com.jessepreiner.scheduling.schedule.protocol.commands.Command;
import com.jessepreiner.scheduling.schedule.protocol.events.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class App {

    private static final ActorSystem<Command> scheduleSystem = ActorSystem.create(Behaviors.empty(), "ScheduleSystem");
    private static AkkaSchedulingService schedulingService = new AkkaSchedulingService(scheduleSystem);

    public static void main(String[] args) {
        startProjection(scheduleSystem);
        Scanner scanner = new Scanner(System.in);

        /* todo
        - solidify protocol -> commands, events, responses, state, and boundaries\
        - schedule supervisor restarts schedule\
        - akka projection for schedule readside
            - serve http GET

        - are schedule reminders state within a schedule, or separate?
        - well-defined ports into system
        */

        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            try {
                System.out.println("Processing line " + line);
                String[] commandParams = line.split("_");
                String command = commandParams[0];
                if (command.equalsIgnoreCase("add")) {
                    processAdd(commandParams[1]);
                } else if (command.equalsIgnoreCase("get")) {
                    processGet(commandParams[1]);
                }

            } catch (Exception e) {
                System.out.printf("Blew up processing line %s due to (%s):  %s\r\n", line, e.getClass(), e.getMessage());
            }
        }
    }

    private static void startProjection(ActorSystem actorSystem) {
        CassandraSession session =
                CassandraSessionRegistry.get(actorSystem).sessionFor("akka.persistence.cassandra");
// use same keyspace for the item_popularity table as the offset store
        String projectionKeyspace =
                actorSystem.settings().config().getString("akka.projection.cassandra.offset-store.keyspace");

        ScheduleProjection.init(actorSystem);

    }

    private static void processAdd(String commands) {
        String[] commandParams = commands.split("\\|");

        String startTime = commandParams[0];
        String endTime = commandParams[1];
        schedulingService.addSchedule(LocalDateTime.parse(startTime), LocalDateTime.parse(endTime)).thenAccept(System.out::println);
    }

    private static void processGet(String commands) {
        String[] commandParams = commands.split("\\|");
        schedulingService.getSchedule(commandParams[0]).thenAccept(System.out::println);
    }

}
