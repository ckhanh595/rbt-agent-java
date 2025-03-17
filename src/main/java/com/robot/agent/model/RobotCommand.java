package com.robot.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RobotCommand(
        String command,
        @JsonProperty("linear_x") double linearX,
        @JsonProperty("angular_z") double angularZ
) {
    @Override
    public String toString() {
        return "RobotCommand{" +
                "command='" + command + '\'' +
                ", linearX=" + linearX +
                ", angularZ=" + angularZ +
                '}';
    }
}