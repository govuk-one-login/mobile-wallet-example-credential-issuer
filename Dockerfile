FROM gradle:8.13-jdk17-alpine@sha256:1a505a603ab40e4eb5385eae3c257576a21af66b5f709a44800f3568293bd981

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run