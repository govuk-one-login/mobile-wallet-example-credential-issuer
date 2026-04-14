# Mobile Wallet Example Credential Issuer

## Project structure

This project contains example issuers that will help you build your own implementation and make sure it meets the standards required by GOV.UK Wallet. This project contains two modules:

* [`document-signing-certificate-issuer`](./document-signing-certificate-issuer/) - An issuer of mDL document signing certificates. Written in Node.js.
* [`example-credential-issuer`](./example-credential-issuer/) - A reference implementation credential issuer to issue credentials into GOV.UK Wallet. Written in Java.

## Contributing

This project uses [pre-commit](https://pre-commit.com/) to enforce code quality and validate commit messages against [Conventional Commits](https://github.com/conventional-changelog/commitlint) standards across 
both [document-signing-certificate-issuer](https://github.com/govuk-one-login/mobile-wallet-example-credential-issuer/tree/main/document-signing-certificate-issuer) and 
[example-credential-issuer](https://github.com/govuk-one-login/mobile-wallet-example-credential-issuer/tree/main/example-credential-issuer) projects.
Non-conforming messages will be rejected.

Ensure your branch is up to date and all hooks pass before opening a pull request. Avoid using the git `--no-verify` flag to skip these checks unless absolutely necessary.

### Installation

```bash
brew install pre-commit
```

```bash
pre-commit install 
pre-commit install --hook-type commit-msg
pre-commit install --hook-type pre-push
```

## Getting Started

See the README files in each module directory for further information on each service.
