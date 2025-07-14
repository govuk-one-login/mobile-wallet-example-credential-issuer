FROM gradle:8.14-jdk17-alpine@sha256:e671d13b5a270e1f869d79c1649381d145dcb48896959f06f412ca124c288f24

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run