FROM gradle:9.6.0-jdk17-alpine@sha256:e5f513d0c3b36d029b72fc83176dd17629653bb8b7eaf6cb98aad0ec7c4b942f

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run