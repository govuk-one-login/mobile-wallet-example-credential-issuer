import { generators } from "openid-client";
import random = generators.random;
import { KmsService } from "../../services/kmsService";
import { base64Encoder } from "../../utils/base64Encoder";
import { Jwt } from "../../types/Jwt";

const TOKEN_TTL_IN_MILLISECONDS = 5 * 60000; // 5 minutes
const TOKEN_SIGNING_ALGORITHM = "RS512";
const TOKEN_JWT_TYPE = "JWT";
const TOKEN_KMS_SIGNING_ALGORITHM = "RSASSA_PKCS1_V1_5_SHA_512";

export async function buildClientAssertion(
  clientId: string,
  tokenEndpoint: string,
  signingKeyId: string,
  kmsService = new KmsService(signingKeyId),
): Promise<Jwt> {
  const header = { alg: TOKEN_SIGNING_ALGORITHM, typ: TOKEN_JWT_TYPE };

  const timeNow = Date.now();
  const payload = {
    iss: clientId,
    sub: clientId,
    aud: tokenEndpoint,
    exp: Math.floor((timeNow + TOKEN_TTL_IN_MILLISECONDS) / 1000),
    iat: Math.floor(timeNow / 1000),
    jti: random(),
  };

  const encodedHeader = base64Encoder(header);
  const encodedPayload = base64Encoder(payload);
  const message = `${encodedHeader}.${encodedPayload}`;
  const signature = await kmsService.sign(message, TOKEN_KMS_SIGNING_ALGORITHM);

  return `${encodedHeader}.${encodedPayload}.${signature}`;
}
