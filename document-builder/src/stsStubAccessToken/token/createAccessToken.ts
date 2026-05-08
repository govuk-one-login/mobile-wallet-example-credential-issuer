import { AccessTokenPayload } from "../types/AccessTokenPayload";
import { Jwt } from "../../types/Jwt";
import { PreAuthorizedCodePayload } from "../types/PreAuthorizedCodePayload";
import { KmsService } from "../../services/kmsService";
import { randomUUID } from "node:crypto";
import { base64Encoder } from "../../utils/base64Encoder";

const ACCESS_TOKEN_SIGNING_ALGORITHM = "ES256";
const ACCESS_TOKEN_JWT_TYPE = "at+jwt";
const ACCESS_TOKEN_KMS_SIGNING_ALGORITHM = "ECDSA_SHA_256";

export async function createAccessToken(
  walletSubjectId: string,
  preAuthorizedCodePayload: PreAuthorizedCodePayload,
  signingKeyId: string,
  accessTokenTtl: number,
  kmsService = new KmsService(signingKeyId),
): Promise<Jwt> {
  const nowInSeconds = Math.floor(Date.now() / 1000);
  const payload: AccessTokenPayload = {
    sub: walletSubjectId,
    iss: preAuthorizedCodePayload.aud,
    aud: preAuthorizedCodePayload.iss,
    credential_identifiers: preAuthorizedCodePayload.credential_identifiers,
    c_nonce: randomUUID(),
    exp: nowInSeconds + accessTokenTtl,
    jti: randomUUID(),
  };

  const header = {
    alg: ACCESS_TOKEN_SIGNING_ALGORITHM,
    typ: ACCESS_TOKEN_JWT_TYPE,
    kid: signingKeyId,
  };

  const encodedHeader = base64Encoder(header);
  const encodedPayload = base64Encoder(payload);

  const message = `${encodedHeader}.${encodedPayload}`;

  const signature = await kmsService.sign(
    message,
    ACCESS_TOKEN_KMS_SIGNING_ALGORITHM,
  );

  return `${encodedHeader}.${encodedPayload}.${signature}`;
}
