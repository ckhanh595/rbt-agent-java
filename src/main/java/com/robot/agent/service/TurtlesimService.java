package com.robot.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robot.agent.model.RobotCommand;
import com.robot.agent.model.StatusMessage;
import id.jros2client.JRos2Client;
import id.jrosclient.TopicSubmissionPublisher;
import id.jrosclient.TopicSubscriber;
import id.jrosmessages.geometry_msgs.PoseMessage;
import id.jrosmessages.geometry_msgs.TwistMessage;
import id.jrosmessages.geometry_msgs.Vector3Message;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class TurtlesimService {

    @Value("${ros2.subscribers}")
    private Set<String> subscribers;

    private static final String TURLESIM_COMMAND_TOPIC = "/turtle1/cmd_vel";

    private static final String TURTLESIM_POSE_TOPIC = "/turtle1/pose";

    private static final String STATUSES_TOPIC = "robot/statuses";

    private final JRos2Client ros2Client;

    private final MqttService mqttService;

    private final ObjectMapper objectMapper;

    private final Map<String, TopicSubmissionPublisher<TwistMessage>> publishers = new HashMap<>();

    public void processCommand(RobotCommand command) {
        log.info("Processing command: {}", command);

        var cmdType = command.command().toLowerCase();
        switch (cmdType) {
            case "move":
                moveRobot(command.linearX(), command.angularZ());
                break;
            case "stop":
                log.warn("Stop command not implemented");
                stopRobot();
                break;
            default:
                log.warn("Unknown command type: {}", cmdType);
                break;
        }
    }

    private TopicSubmissionPublisher<TwistMessage> getOrCreatePublisher(String topicName) {
        return publishers.computeIfAbsent(topicName, topic -> {
            try {
                var publisher = new TopicSubmissionPublisher<>(TwistMessage.class, topic);
                ros2Client.publish(publisher);
                log.info("Created publisher for topic: {}", topic);

                return publisher;
            } catch (Exception e) {
                log.error("Failed to create publisher for topic: {}", topic, e);
                throw new RuntimeException("Failed to create publisher", e);
            }
        });
    }

    private void moveRobot(double linearX, double angularZ) {
        try {
            // Create twist message
            var twist = new TwistMessage();

            // Set linear velocity
            var linear = new Vector3Message();
            linear.withX(linearX);
            linear.withY(0.0);
            linear.withZ(0.0);
            twist.withLinear(linear);

            // Set angular velocity
            var angular = new Vector3Message();
            angular.withX(0.0);
            angular.withY(0.0);
            angular.withZ(angularZ);
            twist.withAngular(angular);

            var publisher = getOrCreatePublisher(TURLESIM_COMMAND_TOPIC);
            log.info("Sent move command to turtlesim: linear.x={}, angular.z={}", linearX, angularZ);
            publisher.submit(twist);

            var statusMessage = new StatusMessage("SUCCESS", "Robot is moving", new RobotCommand("move", linearX, angularZ));
            var status = objectMapper.writeValueAsString(statusMessage);
            mqttService.publish(STATUSES_TOPIC, status);
            log.info("Published status to Robot Agent: {}", status);
        } catch (Exception e) {
            log.error("Error sending move command", e);
        }
    }

    private void stopRobot() {
        try {
            var twist = new TwistMessage();

            var linear = new Vector3Message();
            linear.withX(0.0);
            linear.withY(0.0);
            linear.withZ(0.0);
            twist.withLinear(linear);

            var angular = new Vector3Message();
            angular.withX(0.0);
            angular.withY(0.0);
            angular.withZ(0.0);
            twist.withAngular(angular);

            var publisher = getOrCreatePublisher(TURLESIM_COMMAND_TOPIC);
            publisher.submit(twist);
            log.info("Sent stop command to turtlesim");
        } catch (Exception e) {
            log.error("Error sending stop command", e);
        }
    }

    @PostConstruct
    private void subscribeToTurlesim() {
        ros2Client.subscribe(
                new TopicSubscriber<>(PoseMessage.class, TURTLESIM_POSE_TOPIC) {
                    @Override
                    public void onNext(PoseMessage item) {
                        log.info(item.toString());

                        var subscription = getSubscription();
                        subscription.map(sub -> {
                            sub.request(1);
                            return sub;
                        }).orElseThrow((() -> new IllegalStateException("Subscription not available")));
                    }
                });
    }
}
