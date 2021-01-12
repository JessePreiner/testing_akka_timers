package com.jessepreiner;

import com.jessepreiner.scheduling.AkkaSchedulingService;
import com.jessepreiner.scheduling.SchedulingService;

import java.time.LocalDateTime;
import java.util.Scanner;

public class App {

    private static final SchedulingService schedulingService = new AkkaSchedulingService();

    public static void main(String[] args) {
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
                    processAdd(commandParams[1]);
                } else if (command.equalsIgnoreCase("get")) {
                    processGet(commandParams[1]);
                }

            } catch (Exception e) {
                System.out.printf("Blew up processing line %s due to (%s):  %s\r\n", line, e.getClass(), e.getMessage());
            }
        }
    }

    private static void processAdd(String commands) {
        String[] commandParams = commands.split("\\|");

        String startTime = commandParams[0];
        String endTime = commandParams[1];
        schedulingService.addSchedule(LocalDateTime.parse(startTime), LocalDateTime.parse(endTime)).thenAccept(System.out::println);
    }

    private static void processGet(String commands) {
        String[] commandParams = commands.split("\\|");
        schedulingService.getSchedule(commandParams[0]);
    }

}
