FROM gradle:9.5.1-jdk17-alpine@sha256:ba0ed0695be942cab210cbdcf2095dded443ec19aa6bdc73c0dc7c0cbd95ad93

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run