#FROM eclipse-temurin:17-jre-jammy
#
#WORKDIR /app
#
## Copy the JAR file built by Gradle
#COPY build/libs/*.jar app.jar
#
## ENV SPRING_PROFILES_ACTIVE=production
## Default values that can be overridden
#ENV MQTT_BROKER_URL=192.168.110.61
#ENV MQTT_PORT=1883
#
#EXPOSE 8081
#
## Run the application
#ENTRYPOINT ["java", "-jar", "app.jar"]

FROM ros:humble-ros-base

# Install Java
RUN apt-get update && apt-get install -y openjdk-17-jre-headless

WORKDIR /app

COPY build/libs/*.jar app.jar

ENV MQTT_BROKER_URL=host-ip
ENV MQTT_PORT=1883

EXPOSE 8081

ENTRYPOINT ["bash", "-c", "source /opt/ros/humble/setup.bash && java -jar app.jar"]