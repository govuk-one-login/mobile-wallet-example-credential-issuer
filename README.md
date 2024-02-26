# mobile-wallet-example-credential-issuer

## Overview

A Mock Wallet CRI stub for testing the GOV.UK wallet.

## Pre-requisites

### SDKMan
This project has an `.sdkmanrc` file.

Install SDKMan via the instructions on `https://sdkman.io/install`.

For auto-switching between JDK versions, edit your `~/.sdkman/etc/config` and set `sdkman_auto_env=true`.

Then use sdkman to install Java JDK listed in this project's `.sdkmanrc`, e.g. `sdk install java x.y.z-amzn`.

Restart your terminal.

### Gradle
Gradle 8 is used on this project.

## Quickstart

### Build
Build with `./gradlew`

### Run server locally
Run with `./gradlew run`

Visit localhost:8080/hello/{name} to check the app is running as expected.
