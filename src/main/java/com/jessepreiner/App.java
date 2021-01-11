package com.jessepreiner;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.receptionist.ServiceKey;
import com.jessepreiner.scheduling.schedule.ScheduleActor;
import com.jessepreiner.scheduling.schedule.ScheduleData;
import com.jessepreiner.scheduling.schedule.ScheduleSupervisor;
import com.jessepreiner.scheduling.schedule.protocol.commands.AddScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.commands.Command;
import com.jessepreiner.scheduling.schedule.protocol.commands.RetrieveScheduleCommand;
import com.jessepreiner.scheduling.schedule.protocol.events.ScheduleAddedEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

import static com.jessepreiner.scheduling.schedule.ScheduleActor.*;

public class App {

    private static final Duration ASK_TIMEOUT = Duration.ofSeconds(5);

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ActorSystem<Command> guardian = ActorSystem.create(ScheduleSupervisor.create(), "Empty");

        /* todo
        - solidify protocol -> commands, events, responses, state, and boundaries
        - one actor per schedule
        - schedule supervisor restarts schedule
        - introduce persistence + sharding
        - akka projection for schedule readside

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
                    processAdd(guardian, commandParams[1]);
                } else if (command.equalsIgnoreCase("get")) {
                    processGet(guardian, commandParams[1]);
                }

            } catch (Exception e) {
                System.out.printf("Blew up processing line %s due to (%s):  %s\r\n", line, e.getClass(), e.getMessage());
            }
        }
    }

    private static void processAdd(ActorSystem<Command> guardian, String commands) {
        String[] commandParams = commands.split("\\|");

        String startTime = commandParams[0];
        String endTime = commandParams[1];
        AskPattern.ask(
                guardian,
                replyTo -> new AddScheduleCommand(LocalDateTime.parse(startTime), LocalDateTime.parse(endTime), replyTo),
                ASK_TIMEOUT,
                guardian.scheduler()
        ).whenComplete((reply, failure) -> {
            if (reply instanceof ScheduleAddedEvent) {
                System.out.println("Schedule added " + reply.toString());
            } else if (failure != null) {
                System.out.println("Got failure " + failure.getMessage());
            }
        });
    }

    private static void processGet(ActorSystem<Command> guardian, String commands) {
        String[] commandParams = commands.split("\\|");

        ServiceKey<Command> scheduleActorKey = ScheduleActor.scheduleActorKey;

        String scheduleId = commandParams[0];
        AskPattern.ask(
                guardian,
                replyTo -> new RetrieveScheduleCommand(scheduleId, replyTo),
                ASK_TIMEOUT,
                guardian.scheduler())
                  .whenComplete((reply, failure) -> {
                      if (reply instanceof ScheduleData) {
                          System.out.println("Retrieved schedule " + reply.toString());
                      } else if (failure != null) {
                          System.out.println("Got failure " + failure.getMessage());
                      }
                  });
    }

}
