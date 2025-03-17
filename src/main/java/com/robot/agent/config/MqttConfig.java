package com.robot.agent.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {

    @Bean
    public MqttClient mqttClient() throws MqttException {
        var broker = "tcp://localhost:1883";
        var clientId = "RobotManager_" + System.currentTimeMillis();
        var client = new MqttClient(broker, clientId);
        var options = new MqttConnectOptions();
        options.setAutomaticReconnect(true); // Auto-reconnect if connection is lost
        options.setConnectionTimeout(10);    // Timeout in seconds
        options.setKeepAliveInterval(60);    // Heartbeat check
        client.connect(options);

        return client;
    }
}