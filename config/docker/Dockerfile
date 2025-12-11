FROM eclipse-temurin:25-jdk AS builder
WORKDIR /workspace
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml
RUN ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_DEBUG_OPTS=""
COPY --from=builder /workspace/target/aequitascentralservice-0.1.0-SNAPSHOT.jar app.jar
COPY config/docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
EXPOSE 8080
EXPOSE 5005
ENTRYPOINT ["/entrypoint.sh"]
