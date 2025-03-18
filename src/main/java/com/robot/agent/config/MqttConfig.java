package com.robot.agent.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {
    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.port}")
    private int port;

    @Value("${mqtt.client-id}")
    private String client;

    @Bean
    public MqttClient mqttClient() throws MqttException {
        var broker = "tcp://" + brokerUrl + ":" + port;
        var clientId = client + "_" + System.currentTimeMillis();
        var client = new MqttClient(broker, clientId);
        var options = new MqttConnectOptions();
        options.setAutomaticReconnect(true); // Auto-reconnect if connection is lost
        options.setConnectionTimeout(10);    // Timeout in seconds
        options.setKeepAliveInterval(60);    // Heartbeat check
        client.connect(options);

        return client;
    }
}