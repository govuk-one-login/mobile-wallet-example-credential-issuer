import { KmsService } from "../services/kmsService";
import { createJwkFromRawPublicKey } from "../utils/keyUtils";

export interface Jwks {
  keys: RsaJwksKey[];
}

export interface RsaJwksKey {
  kty: "RSA";
  use: "sig";
  kid: string;
  e: string;
  n: string;
}

export async function buildJwks(
  keyId: string,
  kmsService: KmsService,
): Promise<Jwks> {
  const publicKeyBase64 = await kmsService.getPublicKey();
  const rawPublicKey = Buffer.from(publicKeyBase64, "base64");
  const jwk = createJwkFromRawPublicKey(rawPublicKey);

  if (jwk.kty !== "RSA" || !jwk.n || !jwk.e) {
    throw new Error(`Expected RSA public key but got kty: ${jwk.kty}`);
  }

  return {
    keys: [
      {
        kty: "RSA",
        use: "sig",
        kid: keyId,
        e: jwk.e,
        n: jwk.n,
      },
    ],
  };
}
