FROM node:lts-bookworm-slim AS builder

WORKDIR /workspace

COPY package.json package-lock.json .npmrc ./
RUN npm ci --ignore-scripts
COPY src ./src
COPY tsconfig.json ./
RUN npm run build

FROM node:lts-bookworm-slim
WORKDIR /workspace

COPY --from=builder /workspace/node_modules ./node_modules
COPY --from=builder /workspace/dist ./dist
COPY src ./src
COPY test ./test
COPY jest.config.ts tsconfig.json package.json .npmrc run-server-and-tests.sh run-server.sh ./

# The secure pipeline expects the test script to be at /run-tests.sh
COPY run-tests.sh /run-tests.sh

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    chmod 755 /run-tests.sh && \
    mkdir -p /results && \
    chown -R node:node /workspace /results
USER node

EXPOSE 3001
ENV PORT=3001
ENTRYPOINT ["/run-tests.sh"]
