FROM gradle:9.4-jdk17-alpine@sha256:0cf1585fde964bdcd4551019c52b9f4f4a611d5b031d6599aa60d4ea19f5d096

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run