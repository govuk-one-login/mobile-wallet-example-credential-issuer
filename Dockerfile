FROM gradle:8.8.0-jdk17-alpine@sha256:bbb264cb9b7aca0213f849f9b2f4989edea4b32fe85f026f3852f2f956d08fd4

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run