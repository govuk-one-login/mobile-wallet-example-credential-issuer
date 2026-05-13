import { JWTPayload, decodeJwt } from "jose";
import { PreAuthorizedCodePayload } from "../types/PreAuthorizedCodePayload";
import { logger } from "../../middleware/logger";

export enum GrantType {
  PREAUTHORIZED_CODE = "urn:ietf:params:oauth:grant-type:pre-authorized_code",
}
export function validateGrantType(grantType: string): boolean {
  if (grantType !== GrantType.PREAUTHORIZED_CODE) {
    logger.error("Invalid grant type");
    return false;
  }

  return true;
}

function IsValidIssuer(payload: JWTPayload) {
  return "iss" in payload && typeof payload.iss === "string";
}

function IsValidAudience(payload: JWTPayload) {
  return "aud" in payload && typeof payload.aud === "string";
}

function IsValidCredentialIdentifier(payload: JWTPayload) {
  return (
    Array.isArray(payload.credential_identifiers) &&
    typeof payload.credential_identifiers[0] === "string"
  );
}

export function getPreAuthorizedCodePayload(
  preAuthorizedCode: string,
): false | PreAuthorizedCodePayload {
  const payload: PreAuthorizedCodePayload = decodeJwt(preAuthorizedCode);

  if (!IsValidIssuer(payload)) {
    logger.error("Invalid JWT Issuer");
    return false;
  }

  if (!IsValidAudience(payload)) {
    logger.error("Invalid JWT Audience");
    return false;
  }

  if (!IsValidCredentialIdentifier(payload)) {
    logger.error("Invalid JWT Credential Identifier");
    return false;
  }

  return payload;
}
