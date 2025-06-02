FROM gradle:8.14-jdk17-alpine@sha256:e44214377e536b54512ec0947038184c571cf4b4f0fa44288732670b759ebe7d

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run