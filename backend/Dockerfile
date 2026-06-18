FROM gradle:9.1.0-jdk21 AS build
WORKDIR /home/gradle/project
COPY . .
RUN gradle :backend:installDist --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/backend/build/install/backend /app
EXPOSE 8080
ENTRYPOINT ["/app/bin/backend"]
