package com.robot.agent.config;

import id.jros2client.JRos2Client;
import id.jros2client.JRos2ClientConfiguration;
import id.jros2client.JRos2ClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JRos2ClientConfig {

    @Bean
    public JRos2Client jRos2Client() {
        var configBuilder = new JRos2ClientConfiguration.Builder();

        return new JRos2ClientFactory().createClient(configBuilder.build());
    }
}
