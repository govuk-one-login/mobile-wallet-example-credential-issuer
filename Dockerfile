FROM gradle:8.12.1-jdk17-alpine@sha256:cde9e892ca1f4a98f200b27c6a6dd05a38ca62fc494b9c52977b9404c7485c9d

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run