import { VeteranCardRequestBody } from "../types/VeteranCardRequestBody";
import { validateCredentialExpiryDate } from "../../utils/date";
import { ValidationResult } from "../../types/ValidationResult";
import { CUSTOM_CREDENTIAL_TTL } from "../../config/credentialTtl";

export function validateVeteranCardForm(
  body: VeteranCardRequestBody,
): ValidationResult {
  const errors: Record<string, string> = {};

  if (body.credentialTtl === CUSTOM_CREDENTIAL_TTL) {
    const expiryErrors = validateCredentialExpiryDate(
      body["credentialExpiry-day"],
      body["credentialExpiry-month"],
      body["credentialExpiry-year"],
    );
    Object.assign(errors, expiryErrors);
  }

  if (body.expectedUpdateSeconds && Number.isNaN(Number(body.expectedUpdateSeconds))) {
    errors.expected_update = "Enter a number";
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}
