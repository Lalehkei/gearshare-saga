# Build context is the REPO ROOT -- see docker-compose.yml.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY common-events/pom.xml common-events/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY approval-service/pom.xml approval-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY handover-service/pom.xml handover-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY orchestrator-service/pom.xml orchestrator-service/pom.xml
RUN mvn -q -pl common-events,payment-service -am dependency:go-offline

COPY common-events/src common-events/src
COPY payment-service/src payment-service/src
RUN mvn -q -pl common-events,payment-service -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/payment-service/target/payment-service.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
