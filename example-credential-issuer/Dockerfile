FROM gradle:9.6.1-jdk17-alpine@sha256:5beee04f4c08985b5522ec7c68611fb8744f2fbbd19227305f544e6beebba876

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run