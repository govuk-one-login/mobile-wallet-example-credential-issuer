import { JWK } from "jose";
import { jwksSchema } from "./jwksSchema";
import { getAjvInstance } from "../ajv/ajvInstance";

export interface JWKS {
  keys: JWK[];
}

export async function isValidJwks(jwks: JWKS) {
  const ajv = getAjvInstance();
  const rulesValidator = ajv.compile(jwksSchema);
  if (!rulesValidator(jwks)) {
    const message = JSON.stringify(rulesValidator.errors);
    throw new Error(
      `INVALID_JWKS: JWKS does not comply with the schema. ${message}`,
    );
  }
  return true;
}
