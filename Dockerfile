FROM eclipse-temurin:25-jdk-jammy as builder
WORKDIR /workspace
COPY pom.xml pom.xml
RUN ./mvnw -q -B dependency:go-offline
COPY src src
RUN ./mvnw -q -B package -DskipTests

FROM gcr.io/distroless/java25-debian12:nonroot
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=builder /workspace/target/aequitascentralservice-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
