import {
  decodeProtectedHeader,
  importJWK,
  JWTPayload,
  jwtVerify,
  JWTVerifyResult,
  ProtectedHeaderParameters,
} from "jose";
import { headerSchema } from "./headerSchema";
import { VerificationMethod } from "../../didDocument/isValidDidWebDocument";
import { payloadSchema } from "./payloadSchema";
import { getAjvInstance } from "../../ajv/ajvInstance";

export async function isValidCredential(
  credential: string,
  didKey: string,
  verificationMethods: VerificationMethod[],
  criUrl: string,
): Promise<true> {
  const header: ProtectedHeaderParameters = getHeaderClaims(credential);
  const { payload } = await verifySignature(
    verificationMethods,
    header,
    credential,
  );
  validatePayload(payload, didKey, criUrl);
  return true;
}

function getHeaderClaims(jwt: string): ProtectedHeaderParameters {
  let claims: ProtectedHeaderParameters;
  try {
    claims = decodeProtectedHeader(jwt);
  } catch (error) {
    throw new Error(
      `INVALID_HEADER: Failed to decode credential header. ${error}`,
    );
  }

  const ajv = getAjvInstance();
  const rulesValidator = ajv.compile(headerSchema);
  if (rulesValidator(claims)) {
    return claims;
  } else {
    throw new Error(
      `INVALID_HEADER: Credential header does not comply with the schema. ${JSON.stringify(rulesValidator.errors)}`,
    );
  }
}

async function verifySignature(
  verificationMethods: VerificationMethod[],
  header: ProtectedHeaderParameters,
  credential: string,
): Promise<JWTVerifyResult> {
  const verificationMethod = verificationMethods.find(
    (item) => item.id === header.kid!,
  );
  if (!verificationMethod) {
    throw new Error(
      "INVALID_SIGNATURE: No public key found in DID for provided 'kid'",
    );
  }
  const publicKey = await importJWK(
    verificationMethod.publicKeyJwk,
    header.alg,
  );
  try {
    return await jwtVerify(credential, publicKey);
  } catch (error) {
    throw new Error(
      `INVALID_SIGNATURE: Credential verification failed. ${JSON.stringify(error)}`,
    );
  }
}

function validatePayload(
  payload: JWTPayload,
  didKey: string,
  criUrl: string,
): void {
  const ajv = getAjvInstance();
  const rulesValidator = ajv.compile(payloadSchema);
  if (!rulesValidator(payload)) {
    throw new Error(
      `INVALID_PAYLOAD: Credential payload does not comply with the schema. ${JSON.stringify(rulesValidator.errors)}`,
    );
  }

  const iss = payload.iss;
  if (criUrl !== iss) {
    throw new Error(
      `INVALID_PAYLOAD: Invalid "iss" value in token. Should be ${criUrl} but found ${JSON.stringify(iss)}`,
    );
  }

  const sub = payload.sub;
  if (didKey !== sub) {
    throw new Error(
      `INVALID_PAYLOAD: Invalid "sub" value in token. Should be ${didKey} but found ${JSON.stringify(sub)}`,
    );
  }
}
