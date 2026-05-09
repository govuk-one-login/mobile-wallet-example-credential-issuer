import { Client } from "openid-client";

declare module "express-serve-static-core" {
  interface Request {
    oidc: Client;
  }
}
