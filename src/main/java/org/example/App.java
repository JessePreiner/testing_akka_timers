package org.example;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

import static org.example.ScheduleBehavior.ScheduleCommand;
import static org.example.ScheduleBehavior.create;

public class App {

    public static void main(String[] args) {
        ActorSystem<ScheduleCommand> guardian = ActorSystem.create(create(), "Empty");
        Scanner scanner = new Scanner(System.in);

        /* todo
        - introduce persistence + sharding
        - one actor per schedule
        - auto passivation
        - schedule supervisor restarts schedules
        - are schedule reminders state within a schedule, or separate?
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

    private static void processGet(ActorSystem<ScheduleCommand> guardian, String commands) {
        String[] commandParams = commands.split("\\|");

        String scheduleId = commandParams[0];
        AskPattern.ask(
                guardian,
                replyTo -> new RetrieveSchedule(scheduleId, replyTo),
                Duration.ofSeconds(2),
                guardian.scheduler())
                  .whenComplete((reply, failure) -> {
                      if (reply instanceof ScheduleData) {
                          System.out.println("Retrieved schedule " + reply.toString());
                      } else if (failure != null) {
                          System.out.println("Got failure " + failure.getMessage());
                      }
                  });
    }

    private static void processAdd(ActorSystem<ScheduleCommand> guardian, String commands) {
        String[] commandParams = commands.split("\\|");

        String startTime = commandParams[0];
        String endTime = commandParams[1];
        AskPattern.ask(
                guardian,
                replyTo -> new AddSchedule(LocalDateTime.parse(startTime), LocalDateTime.parse(endTime), replyTo),
                Duration.ofSeconds(2),
                guardian.scheduler())
                  .whenComplete((reply, failure) -> {
                      if (reply instanceof ScheduleAddedEvent) {
                          System.out.println("Schedule added " + reply.toString());
                      } else if (failure != null) {
                          System.out.println("Got failure " + failure.getMessage());
                      }
                  });
    }

}
