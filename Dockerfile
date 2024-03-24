FROM gradle:8.6.0-jdk17-alpine@sha265:efc3f440f6a8685bedd93e888bbda0ba82afc4b3

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yaml ./
COPY gradle/ gradle/
ARG PORT
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run