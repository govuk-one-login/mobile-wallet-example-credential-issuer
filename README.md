# Mobile Wallet Document Builder

## Overview

A service for creating and storing test documents used by the GOV.UK Wallet credential issuer to issue the corresponding digital credentials. Once a document is created, the service displays the credential offer returned by the issuer, which can then be consumed by GOV.UK Wallet.

## Tech Stack

This service is built with TypeScript and Node.js/Express, using Nunjucks for server-side templating, containerised with Docker, and deployed to ECS Fargate behind an API Gateway. It uses DynamoDB and S3 for storage and KMS for signing and encryption, with infrastructure managed via AWS SAM.

## Prerequisites

- [Node.js](https://nodejs.org/en) — we recommend managing versions with [nvm](https://github.com/nvm-sh/nvm)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) — required to run LocalStack locally and to build the app image

## Local Setup

### Install

```bash
npm install
```

### Pre-commit Hooks

This project uses [Husky](https://typicode.github.io/husky/) to run checks before commits and pushes. Set it up with:

```bash
npm run setup-hooks
```

### Checkov

We use Checkov for static analysis of our IaC. Following can be used to run a Checkov analysis locally.

```bash
brew install checkov

# Running Checkov analysis
npm run checkov
```
### Lint & Format

```bash
npm run lint:fix
npm run format
```

### Build

```bash
npm run build
```

### Run

Create a local environment file:

```bash
cp .env.example .env
```

Start LocalStack to emulate AWS services (DynamoDB, S3, KMS) on port `4561`:

```bash
npm run localstack:up
```

Running locally also requires the example credential issuer and STS mock for end-to-end journey functionality.

> **Note:** Authentication is disabled in `local` and `integration` environments. It is only active in `dev`, `build`, and `staging`.

Start the application:

```bash
npm run start     # production mode
npm run dev       # development mode with hot reload
```

The service will be available at http://localhost:8001/start (standard journey) or http://localhost:8001/dvs/start (DVS journey).

### Test

```bash
npm run test
```

## Deployment

This service is deployed via GitHub Actions.

Automated deployments to `build` are triggered on push to `main` after PR approval. Manual deployments to `dev` can be triggered from the GitHub Actions menu, where you can specify a branch name or commit SHA.

## Contributing

Pre-commit hooks are used to maintain code quality and validate commit messages against the [Conventional Commits](https://github.com/conventional-changelog/commitlint) standard — non-conforming messages will be rejected.

Ensure your branch is up to date and all hooks pass before opening a pull request. Avoid using the git `--no-verify` flag to skip these checks unless absolutely necessary.

## Further Documentation

| Document                                           | Description |
|----------------------------------------------------|---|
| [`docs/infrastructure.md`](docs/infrastructure.md) | Infrastructure diagram — AWS architecture, API routes, data flow |