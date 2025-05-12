FROM gradle:8.14-jdk17-alpine@sha256:15c6036dff0326604cc7079885015b272e127652618fda93cd1dc3f79e0bdbed

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run