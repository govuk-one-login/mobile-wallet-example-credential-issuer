FROM node:lts-alpine AS builder
WORKDIR /app
COPY src/ src/
COPY package.json package-lock.json tsconfig.json .npmrc ./
RUN npm install --ignore-scripts && npm run build

FROM node:lts-alpine

RUN apk update && apk upgrade busybox

WORKDIR /app
COPY --from=builder /app/dist dist/
COPY --from=builder /app/node_modules node_modules/
COPY package.json ./
ARG PORT
RUN addgroup -S nonroot \
&& adduser -S nonroot -G nonroot
USER nonroot

EXPOSE $PORT

ENTRYPOINT ["npm", "start"]
