FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build --refresh-dependencies --no-daemon --parallel --continue --build-cache -x test

RUN ./gradlew clean bootJar -x test --no-daemon
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
