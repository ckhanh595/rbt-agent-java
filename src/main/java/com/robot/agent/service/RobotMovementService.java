package com.robot.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robot.agent.model.RobotCommand;
import com.robot.agent.model.StatusMessage;
import id.jros2client.JRos2Client;
import id.jros2messages.sensor_msgs.JointStateMessage;
import id.jrosclient.TopicSubmissionPublisher;
import id.jrosclient.TopicSubscriber;
import id.jrosmessages.geometry_msgs.TwistMessage;
import id.jrosmessages.geometry_msgs.Vector3Message;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class RobotMovementService {

    private static final String TURTLESIM_CMD_TOPIC = "/turtle1/cmd_vel";
    private static final String TURTLESIM_POSE_TOPIC = "/turtle1/pose";
    private static final String GAZEBO_CMD_TOPIC = "/cmd_vel";
    private static final String GAZEBO_POSE_TOPIC = "/odom";
    private static final String STATUSES_TOPIC = "robot/statuses";

    private final JRos2Client ros2Client;
    private final MqttService mqttService;
    private final ObjectMapper objectMapper;
    private final Map<String, TopicSubmissionPublisher<TwistMessage>> publishers = new HashMap<>();

    @Value("${ros2.use-turtlesim}")
    private boolean useTurtlesim;

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

            // Select topic based on configuration
            String topicName = useTurtlesim ? TURTLESIM_CMD_TOPIC : GAZEBO_CMD_TOPIC;
            var publisher = getOrCreatePublisher(topicName);

            log.info("Sent move command to {}: linear.x={}, angular.z={}", topicName, linearX, angularZ);
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

            // Select topic based on configuration
            String topicName = useTurtlesim ? TURTLESIM_CMD_TOPIC : GAZEBO_CMD_TOPIC;
            var publisher = getOrCreatePublisher(topicName);
            publisher.submit(twist);
            log.info("Sent stop command to {}", topicName);
        } catch (Exception e) {
            log.error("Error sending stop command", e);
        }
    }

    @PostConstruct
    private void subscribeToRobotPose() {
        log.info("Subscribing to topic: {}", GAZEBO_POSE_TOPIC);

//        var postTopic = useTurtlesim? TURTLESIM_POSE_TOPIC : GAZEBO_POSE_TOPIC;
        var postTopic = "/joint_states";

        try {
            ros2Client.subscribe(
                    new TopicSubscriber<>(JointStateMessage.class, postTopic) {
                        @Override
                        public void onNext(JointStateMessage item) {
                            log.info("JointStateMessage pose: {}", item);

                            var subscription = getSubscription();
                            subscription.map(sub -> {
                                sub.request(1);
                                return sub;
                            }).orElseThrow((() -> new IllegalStateException("Subscription not available")));
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            log.error("Error in TurtleSim pose subscription: {}", throwable.getMessage(), throwable);
                            super.onError(throwable);
                        }
                    });
        } catch (Exception e) {
            log.error("Failed to subscribe to {}: {}", postTopic, e.getMessage(), e);
        }
    }
}
