FROM gradle:9.8.2-jdk17-alpine@sha256:df07a41e7dd3d8e4bcaaae1e04ae98d44058cfbe06666cf472d5c6642c889e2c AS build

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew installDist

FROM eclipse-temurin:25-jre-alpine@sha256:3c0a9084927a221ccd1d007fcaf614465672c0af37aaa834c5184483afe56d61

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
