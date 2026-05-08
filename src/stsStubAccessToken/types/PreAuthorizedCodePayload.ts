import { JWTPayload } from "jose";

export interface PreAuthorizedCodePayload extends JWTPayload {
  aud: string;
  iss: string;
  credential_identifiers: string[];
}
