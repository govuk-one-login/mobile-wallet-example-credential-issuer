import { exportJWK, generateKeyPair } from "jose";
import { writeFile } from "node:fs/promises";
import { getKeyId } from "./config";

export async function initialiseKeyPair() {
  const keyPair = await generateKeyPair("ES256", {
    extractable: true,
  });

  // Required for signing mock STS access token
  const privateKey = await exportJWK(keyPair.privateKey);

  // Required by the CRI to verify the mock STS access token signature - available from /.well-known/jwks.json
  const publicKey = await exportJWK(keyPair.publicKey);
  publicKey.kid = getKeyId(); // add 'kid' to JWK

  await Promise.all([
    writeFile("test/helpers/credential/privateKey", JSON.stringify(privateKey)),
    writeFile("test/helpers/credential/publicKey", JSON.stringify(publicKey)),
  ]);

  return publicKey;
}
