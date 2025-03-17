package com.robot.agent.model;

public record TurtlePose(
        double x,
        double y,
        double theta,
        double linearVelocity,
        double angularVelocity
) {
    @Override
    public String toString() {
        return String.format("TurtlePose[x=%.2f, y=%.2f, theta=%.2f, linearVel=%.2f, angularVel=%.2f]",
                x, y, theta, linearVelocity, angularVelocity);
    }
}