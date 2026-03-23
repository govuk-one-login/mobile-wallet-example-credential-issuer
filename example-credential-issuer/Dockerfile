FROM gradle:9.4-jdk17-alpine@sha256:793a9fde825fb5afa96c897ed288f025ba4dbbe9a5a8adcca3d28bfea3f3b4bc

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run