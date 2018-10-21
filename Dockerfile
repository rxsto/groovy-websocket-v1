FROM openjdk:8
WORKDIR /opt/groovy/websocket
ENTRYPOINT ["java", "-jar", "/opt/groovy/websocket/websocket.jar"]