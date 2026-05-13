import { importJWK, SignJWT, JWK, JWTPayload } from "jose";
import { getKeyId } from "../../../src/config";
import { randomUUID } from "node:crypto";

export interface AccessToken {
  access_token: string;
  token_type: string;
  expires_in: number;
}

const SIGNING_ALGORITHM = "ES256";
const TTL = 180;

export async function createAccessToken(
  c_nonce: string,
  walletSubjectId: string,
  preAuthorizedCodePayload: JWTPayload,
  signingKey: JWK,
): Promise<AccessToken> {
  const signingKeyAsKeyLike = await importJWK(signingKey, SIGNING_ALGORITHM);
  const customClaims = {
    credential_identifiers: preAuthorizedCodePayload.credential_identifiers!,
    c_nonce: c_nonce,
  };
  const nowInSeconds = Math.floor(Date.now() / 1000);
  const accessToken = await new SignJWT(customClaims)
    .setProtectedHeader({
      alg: SIGNING_ALGORITHM,
      typ: "at+jwt",
      kid: getKeyId(),
    })
    .setSubject(walletSubjectId)
    .setIssuer(preAuthorizedCodePayload.aud! as string)
    .setAudience(preAuthorizedCodePayload.iss!)
    .setJti(randomUUID())
    .setExpirationTime(nowInSeconds + TTL)
    .sign(signingKeyAsKeyLike);

  return {
    access_token: accessToken,
    token_type: "bearer",
    expires_in: TTL,
  };
}
