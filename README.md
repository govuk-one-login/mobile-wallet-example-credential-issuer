# Mobile Wallet Example Credential Issuer

## Project structure

This project contains example issuers that will help you build your own implementation and make sure it meets the standards required by GOV.UK Wallet. This project contains the following modules:

* [`document-signing-certificate-issuer`](./document-signing-certificate-issuer/) - An issuer of mDL document signing certificates. Written in Node.js.
* [`example-credential-issuer`](./example-credential-issuer/) - A reference implementation credential issuer to issue credentials into GOV.UK Wallet. Written in Java.
* [`document-builder`](./document-builder) - A service for creating and storing test documents used by the GOV.UK Wallet credential issuer to issue the corresponding digital credentials. Written in Node.js.
* [`test-harness`](./test-harness) - The GOV.UK Wallet Test Harness lets you validate your credential issuance implementation without using the GOV.UK One Login app.
* [`platform-ca`](./platform-ca/) - A private certificate authority that acts as the Issuing Authority Certificate Authority (IACA) for example mdoc-based credentials. Deployed as a CloudFormation stack.

## Contributing

This project uses [pre-commit](https://pre-commit.com/) to enforce code quality and validate commit messages against [Conventional Commits](https://github.com/conventional-changelog/commitlint) standards across all projects in this repository. Non-conforming messages will be rejected.

Ensure your branch is up to date and all hooks pass before opening a pull request. Avoid using the git `--no-verify` flag to skip these checks unless absolutely necessary.

### Installation

Pre-commit 

```bash
brew install pre-commit
```

```bash
pre-commit install 
pre-commit install --hook-type commit-msg
pre-commit install --hook-type pre-push
```

Checkov

We use Checkov for static analysis of our IaC. Following can be used to run a Checkov analysis locally.

```bash
brew install checkov

# Running Checkov analysis
checkov --file <PATH_TO_TEMPLATE>/template.yaml
```

Individual project READMEs have specific commands to run checkov analysis.

## Getting Started

See the README file in each module directory for further information on each service.
