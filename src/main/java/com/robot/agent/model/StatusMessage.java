package com.robot.agent.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record StatusMessage(
        String topic,
        StatusData data
) {
    public StatusMessage(String status, String message, RobotCommand lastCommand) {
        this("robot/statuses", new StatusData(status, message, lastCommand));
    }

    public record StatusData(
            String status,
            String message,
            String timestamp,
            RobotCommand lastCommand
    ) {
        public StatusData(String status, String message, RobotCommand lastCommand) {
            this(status, message, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), lastCommand);
        }
    }
}