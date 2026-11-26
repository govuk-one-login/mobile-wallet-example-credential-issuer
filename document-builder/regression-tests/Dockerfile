FROM node:22-bookworm-slim

LABEL org.opencontainers.image.title="Document Builder regression test image"
LABEL org.opencontainers.image.description="Playwright regression test image consumed by the Document Builder SAM pipeline Test action"
LABEL org.opencontainers.image.source="https://github.com/govuk-one-login/mobile-wallet-example-credential-issuer"

RUN adduser --disabled-password --gecos "" --home /regression-tests test

WORKDIR /regression-tests

COPY package.json package-lock.json ./
RUN npm ci --ignore-scripts --no-audit --no-fund

# Install Chromium and its OS-level dependencies (requires Debian/Ubuntu)
RUN npx playwright install chromium --with-deps

RUN apt-get install -y --no-install-recommends curl unzip \
    && curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" \
    && unzip awscliv2.zip && ./aws/install \
    && rm -rf awscliv2.zip aws \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY playwright.config.ts ./
COPY tests/ ./tests/
COPY run-tests.sh /

RUN chmod +x /run-tests.sh && chown -R test:test /regression-tests

USER test

ENTRYPOINT ["/run-tests.sh"]
