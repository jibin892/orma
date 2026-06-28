FROM gradle:9.1.0-jdk21 AS build
WORKDIR /home/gradle/project
COPY gradle ./gradle
COPY gradle.properties backend-render-settings.gradle.kts ./
COPY backend ./backend
RUN cp backend-render-settings.gradle.kts settings.gradle.kts \
    && gradle :backend:installDist --no-daemon --no-configuration-cache

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /home/gradle/project/backend/build/install/backend /app
EXPOSE 8090
ENTRYPOINT ["/app/bin/backend"]
