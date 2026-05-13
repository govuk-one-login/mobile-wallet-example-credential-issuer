import { CredentialData } from "../types";
import { decodeCredentialAsJwt } from "./jwtDecoder";
import { decodeCredentialAsCbor } from "./mdocDecoder";

/**
 * Determines if a credential should be decoded as JWT based on its prefix
 * @param credential - The credential string to check
 * @returns True if credential appears to be JWT format
 */
export function isJwtFormat(credential: string): boolean {
  return credential.startsWith("eyJ");
}

/**
 * Processes a credential by determining its format and decoding accordingly
 * @param credential - The credential string to process
 * @returns Decoded credential data
 */
export function processCredential(credential: string): CredentialData {
  return isJwtFormat(credential)
    ? decodeCredentialAsJwt(credential)
    : decodeCredentialAsCbor(credential);
}
