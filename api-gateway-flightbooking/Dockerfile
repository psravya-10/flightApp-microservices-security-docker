FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/api-gateway-flightbooking-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java","-jar","app.jar"]


