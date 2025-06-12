FROM gradle:8.14-jdk17-alpine@sha256:3bcf086f7be97f501c749eba9f276a5a51920f4dd3d21b762bf02048e0a6ad22

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run