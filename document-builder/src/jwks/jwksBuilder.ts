import { KmsService } from "../services/kmsService";
import { createJwkFromRawPublicKey } from "../utils/keyUtils";

export interface Jwks {
  keys: JwksKey[];
}

export interface JwksKey {
  kty: string;
  use: string;
  kid: string;
  e?: string;
  n?: string;
}

export async function buildJwks(
  keyId: string,
  kmsService: KmsService,
): Promise<Jwks> {
  const publicKeyBase64 = await kmsService.getPublicKey();
  const rawPublicKey = Buffer.from(publicKeyBase64, "base64");
  const jwk = createJwkFromRawPublicKey(rawPublicKey);

  return {
    keys: [
      {
        kty: jwk.kty!,
        use: "sig",
        kid: keyId,
        e: jwk.e as string,
        n: jwk.n as string,
      },
    ],
  };
}
