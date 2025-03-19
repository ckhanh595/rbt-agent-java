package com.robot.agent.service;

import com.robot.agent.model.RobotCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PatrolService {

    private final RobotMovementService robotService;

    public void startPatrol(String action) {
        log.info("Starting patrol sequence");
        executeNextPatrolStep(action);
    }

    public void stopPatrol() {
        log.info("Stopping patrol sequence");
        robotService.processCommand(new RobotCommand("stop", 0.0, 0.0));
    }

    private void executeNextPatrolStep(String action) {
        switch (action) {
            case "move":
                // Move forward
                robotService.processCommand(new RobotCommand("move", 0.2, 0.0));
                break;
            case "right":
                // Turn right
                robotService.processCommand(new RobotCommand("move", 0.0, -0.5));
                break;
            case "stop":
                // Move forward
                robotService.processCommand(new RobotCommand("stop", 0.0, 0.0));
                break;
            case "left":
                // Turn left
                robotService.processCommand(new RobotCommand("move", 0.0, 0.5));
                break;

        }
    }

}