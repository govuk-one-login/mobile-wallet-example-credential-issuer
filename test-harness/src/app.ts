import express, { Application } from "express";
import { initialiseKeyPair } from "./initialiseKeyPair.js";
import { buildJwksResponse } from "./buildJwksResponse.js";

const JWKS_ROUTE = "/.well-known/jwks.json";

export async function createApp(): Promise<express.Application> {
  const app: Application = express();
  app.disable("x-powered-by");

  const publicKey = await initialiseKeyPair();
  app.get(JWKS_ROUTE, buildJwksResponse(publicKey));

  return app;
}
