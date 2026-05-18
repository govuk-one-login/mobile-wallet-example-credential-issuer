import express, { Application, Request, Response } from "express";
import { getKeyId, getPortNumber } from "./config.js";
import { generateKeyPair, exportJWK, JWK } from "jose";
import { writeFileSync } from "node:fs";

const app: Application = express();
const port = getPortNumber();

let publicKey;

app.get("/.well-known/jwks.json", async (_req: Request, res: Response) => {
  publicKey.kid = getKeyId(); // add 'kid' to JWK
  const response = { keys: [] as JWK[] };
  response.keys.push(publicKey);

  res.status(200).json(response);
});

export const server = app
  .listen(port, async () => {
    console.log(`Server is running on port ${port}`);

    const keyPair = await generateKeyPair("ES256", {
      extractable: true,
    });

    // Required for signing mock STS access token
    const privateKey = await exportJWK(keyPair.privateKey);
    writeFileSync(
      "test/helpers/credential/privateKey",
      JSON.stringify(privateKey),
    );

    // Required by the CRI to verify the mock STS access token signature - available from /.well-known/jwks.json
    publicKey = await exportJWK(keyPair.publicKey);
    writeFileSync(
      "test/helpers/credential/publicKey",
      JSON.stringify(publicKey),
    );
  })
  .on("error", (error: Error) => {
    console.log(error, "Unable to start server");
  });
