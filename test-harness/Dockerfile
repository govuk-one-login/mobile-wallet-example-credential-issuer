FROM node:lts-bookworm-slim AS builder
WORKDIR /workspace
COPY package.json package-lock.json .npmrc ./
RUN npm ci --ignore-scirpts
COPY src ./src
COPY tsconfig.json ./
RUN npm run build

FROM node:lts-bookworm-slim
WORKDIR /workspace

COPY --from=builder /workspace/node_modules ./node_modules
COPY --from=builder /workspace/dist ./dist

COPY src ./src
COPY test ./test
COPY jest.config.ts tsconfig.json package.json .npmrc run-server-and-tests.sh run-server.sh run-tests.sh ./

RUN mkdir -p /results && \
    chown -R node:node /workspace /results
USER node

EXPOSE 3001
ENV PORT=3001
ENTRYPOINT ["bash", "run-server-and-tests.sh"]
