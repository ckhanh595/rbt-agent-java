package com.robot.agent.model;

public record CommandMessage(String topic, RobotCommand data) {

    @Override
    public String toString() {
        return "CommandMessage{" +
                "topic='" + topic + '\'' +
                ", data=" + data +
                '}';
    }
}
