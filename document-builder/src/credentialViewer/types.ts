import { JWTPayload } from "jose";
import { Sign1 } from "@auth0/cose";

export interface AccessTokenClaims extends JWTPayload {
  c_nonce?: string;
  aud?: string;
}

export interface CredentialData {
  credentialClaims: unknown;
  credentialSignature?: Sign1;
  credentialSignaturePayload?: unknown;
  credentialClaimsTitle: string;
  x5chain: string;
  x5chainHex: string;
}

export interface ProofData {
  proofJwt: string;
  proofJwtClaims: JWTPayload | undefined;
}
