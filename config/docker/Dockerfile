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
COPY --from=builder /workspace/target/aequitascentralservice-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
