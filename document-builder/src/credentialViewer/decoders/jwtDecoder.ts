import { decodeJwt, JWTPayload } from "jose";
import { logger } from "../../middleware/logger";
import { CredentialData } from "../types";

/**
 * Safely decodes a JWT token with error handling
 * @param token - The JWT token to decode
 * @param errorMessage - Error message to log if decoding fails
 * @returns Decoded JWT payload or undefined if decoding fails
 */
export function safeDecodeJwt(
  token: string,
  errorMessage: string,
): JWTPayload | undefined {
  try {
    return decodeJwt(token);
  } catch (error) {
    logger.error(error, errorMessage);
    return undefined;
  }
}

/**
 * Decodes a credential as a JWT (VCDM format)
 * @param credential - The credential string to decode
 * @returns Credential data with JWT-specific properties
 */
export function decodeCredentialAsJwt(credential: string): CredentialData {
  const credentialClaims = safeDecodeJwt(
    credential,
    "An error occurred whilst decoding a JWT credential",
  );
  if (credentialClaims) logger.info("Decoded JWT credential");

  return {
    credentialClaims,
    credentialClaimsTitle: "VCDM credential",
    x5chain: "",
    x5chainHex: "",
  };
}
