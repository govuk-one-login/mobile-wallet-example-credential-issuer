import { PreAuthorizedCodePayload } from "./PreAuthorizedCodePayload";
import { UUID } from "node:crypto";

export interface AccessTokenPayload extends PreAuthorizedCodePayload {
  sub: string;
  c_nonce: UUID;
  exp: number;
  jti: UUID;
}
