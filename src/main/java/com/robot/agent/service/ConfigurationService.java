package com.robot.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robot.agent.entity.AgentConfigurationEntity;
import com.robot.agent.repository.ConfigurationRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationService {

    private static final String CONFIGURATIONS_TOPIC = "/robot/configurations";

    private final MqttService mqttService;

    private final ConfigurationRepository configurationRepository;

    private final ObjectMapper objectMapper;

    @PostConstruct
    @SneakyThrows
    public void init() {
        mqttService.subscribe(CONFIGURATIONS_TOPIC, message -> {
            var payload = new String(message.getPayload());
            log.info("Received configuration: {}", payload);

            try {
                var configuration = objectMapper.readValue(payload, AgentConfigurationEntity.class);
                saveConfiguration(configuration);
            } catch (Exception e) {
                log.error("Error processing configuration", e);
            }
        });
    }

    public void saveConfiguration(AgentConfigurationEntity configuration) {
        log.info("Saving configuration: {}", configuration);
        configurationRepository.save(configuration);
    }
    
    public AgentConfigurationEntity getLatestConfiguration() {
        return configurationRepository.findTopByOrderByCreatedAtDesc();
    }
}