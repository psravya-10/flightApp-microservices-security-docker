@echo off
echo Starting Flight Booking Microservices...

start cmd /k java -jar eureka-server-flightbooking\target\eureka-server-flightbooking-0.0.1-SNAPSHOT.jar

timeout /t 30

start cmd /k java -jar config-server-flightbooking\target\config-server-flightbooking-0.0.1-SNAPSHOT.jar

timeout /t 40

start cmd /k java -jar auth-service-flightbooking\target\auth-service-flightbooking-0.0.1-SNAPSHOT.jar

timeout /t 10

start cmd /k java -jar flight-service-flightbooking\target\flight-service-flightbooking-0.0.1-SNAPSHOT.jar

timeout /t 10

start cmd /k java -jar booking-service-flightbooking\target\booking-service-flightbooking-0.0.1-SNAPSHOT.jar

timeout /t 10

start cmd /k java -jar api-gateway-flightbooking\target\api-gateway-flightbooking-0.0.1-SNAPSHOT.jar

echo All services started.
