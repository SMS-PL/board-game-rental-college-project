# --- ETAP 1: Budowanie (Wielka fabryka z JDK i Gradle) ---
FROM gradle:8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Budujemy aplikację bez uruchamiania testów, żeby było szybciej
RUN gradle build -x test --no-daemon

# --- ETAP 2: Uruchamianie (Lekki kontener tylko z JRE) ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Kopiujemy gotowy plik .jar z "fabryki" (Etapu 1)
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
