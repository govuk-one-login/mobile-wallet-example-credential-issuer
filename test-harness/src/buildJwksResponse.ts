import { JWK } from "jose";
import { Request, Response } from "express";

export function buildJwksResponse(publicKey: JWK) {
  return async function expressCallback(_req: Request, res: Response) {
    if (!publicKey) {
      res.status(500).json({ error: "Public key not initialised" });
      return;
    }

    const response = { keys: [] as JWK[] };
    response.keys.push(publicKey);

    res.status(200).json(response);
  };
}
