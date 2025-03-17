package com.robot.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robot.agent.model.RobotCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttRos2BridgeService {

    private static final String COMMANDS_TOPIC = "robot/commands";

    private final MqttService mqttService;

    private final TurtlesimService turtlesimService;

    private final ObjectMapper objectMapper;

    @PostConstruct
    @SneakyThrows
    public void init() {
        mqttService.subscribe(COMMANDS_TOPIC, message -> {
            var payload = new String(message.getPayload());
            log.info("Received command: {}", payload);

            try {
                var robotCommand = objectMapper.readValue(payload, RobotCommand.class);
                turtlesimService.processCommand(robotCommand);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
