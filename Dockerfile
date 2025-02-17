FROM gradle:8.12.1-jdk17-alpine@sha256:8dbab10c7aeaf06c8b486555c76e3dcdb3181c47f90a57ea325e8722858a6759

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run