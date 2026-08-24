FROM gradle:9.7.0-jdk17-alpine@sha256:d537e24fca45130a3d10f4cbe91018cfd6bca92b7fade34ed57f807afedaeb9a AS build

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew installDist

FROM eclipse-temurin:25-jre-alpine@sha256:28db6fdf60e38945e43d840c0333aeaec66c15943070104f7586fd3c9d1665b0

RUN apk update && apk upgrade

RUN addgroup -S nonroot \
    && adduser -S nonroot -G nonroot

WORKDIR /app
COPY --from=build /app/build/install/example-credential-issuer/lib/ /app/lib/
COPY --from=build /app/config.yml /app/config.yml

RUN chown -R nonroot:nonroot /app
USER nonroot

EXPOSE 8080
CMD ["java", "-cp", "lib/*", "uk.gov.di.mobile.wallet.cri.ExampleCriApp", "server", "config.yml"]
