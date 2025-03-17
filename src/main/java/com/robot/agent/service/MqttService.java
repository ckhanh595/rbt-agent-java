package com.robot.agent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
@Slf4j
@RequiredArgsConstructor
public class MqttService {

    private final MqttClient mqttClient;

    public void publish(String topic, String message) throws MqttException {
        var mqttMessage = new MqttMessage(message.getBytes());
        log.info("Publishing message to topic {}: {}", topic, message);

        mqttClient.publish(topic, mqttMessage);
    }

    public void subscribe(String topic, Consumer<MqttMessage> messageHandler) throws MqttException {
        mqttClient.subscribe(topic, (topic1, message) -> messageHandler.accept(message));
    }
}
