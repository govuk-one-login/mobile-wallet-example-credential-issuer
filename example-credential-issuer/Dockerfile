FROM gradle:8.14-jdk17-alpine@sha256:80350e92aa62cc28ec3f1e073a423d5ebe22ff4eea41d44b53c28064685e0364

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew build -x test

EXPOSE 8080
CMD ./gradlew run