package com.jessepreiner;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

import com.jessepreiner.schedule.protocol.events.ScheduleAddedEvent;
import com.jessepreiner.schedule.ScheduleBehavior;
import com.jessepreiner.schedule.ScheduleData;
import com.jessepreiner.schedule.protocol.commands.AddScheduleCommand;
import com.jessepreiner.schedule.protocol.commands.Command;
import com.jessepreiner.schedule.protocol.commands.RetrieveScheduleCommand;

public class App {

    public static void main(String[] args) {
        ActorSystem<Command> guardian = ActorSystem.create(ScheduleBehavior.create(), "Empty");
        Scanner scanner = new Scanner(System.in);

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

    private static void processGet(ActorSystem<Command> guardian, String commands) {
        String[] commandParams = commands.split("\\|");

        String scheduleId = commandParams[0];
        AskPattern.ask(
                guardian,
                replyTo -> new RetrieveScheduleCommand(scheduleId, replyTo),
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

    private static void processAdd(ActorSystem<Command> guardian, String commands) {
        String[] commandParams = commands.split("\\|");

        String startTime = commandParams[0];
        String endTime = commandParams[1];
        AskPattern.ask(
                guardian,
                replyTo -> new AddScheduleCommand(LocalDateTime.parse(startTime), LocalDateTime.parse(endTime), replyTo),
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
