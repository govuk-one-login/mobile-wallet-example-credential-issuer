# Multi-arch (amd64 + arm64) Gradle image; the alpine variant has no arm64 build.
FROM gradle:9.8.2-jdk17-noble@sha256:bbe3195a4217ea126ce343ca7283c46d12e74361a3788823c3de507a56e63382 AS build

WORKDIR /app
COPY src/ src/
COPY build.gradle settings.gradle gradlew gradlew.bat config.yml ./
COPY gradle/ gradle/
RUN ./gradlew installDist

FROM eclipse-temurin:25-jre-alpine@sha256:e1898fd6a11fc965a994d8ace186fd3585aeb636cf8f05f47a723fcfee0b0918

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
