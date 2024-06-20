FROM gradle:8.6.0-jdk17-alpine@sha256:87f40d50d0015236f5aa95d13399508d70e44bc3d97f3bb80efe9a942957825b

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew

EXPOSE 8080
CMD ./gradlew run