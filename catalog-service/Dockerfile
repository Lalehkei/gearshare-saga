# Build context is the REPO ROOT (see docker-compose.yml), not this directory --
# that's what lets us pull in the common-events module the same way a local `mvn`
# build would, via the Maven reactor.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy every module's pom.xml first (Maven's reactor needs the full <modules> list to
# resolve, even though we only build two of them) so Docker can cache the dependency
# download layer independently of source code changes.
COPY pom.xml .
COPY common-events/pom.xml common-events/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml
COPY approval-service/pom.xml approval-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY handover-service/pom.xml handover-service/pom.xml
COPY booking-service/pom.xml booking-service/pom.xml
COPY orchestrator-service/pom.xml orchestrator-service/pom.xml
RUN mvn -q -pl common-events,catalog-service -am dependency:go-offline

COPY common-events/src common-events/src
COPY catalog-service/src catalog-service/src
RUN mvn -q -pl common-events,catalog-service -am package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/catalog-service/target/catalog-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
