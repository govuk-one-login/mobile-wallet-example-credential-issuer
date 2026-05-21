import { VeteranCardRequestBody } from "../types/VeteranCardRequestBody";
import { validateCredentialExpiryDate } from "../../utils/date";
import { ValidationResult } from "../../types/ValidationResult";

export class VeteranCardFormValidator {
  validate(body: VeteranCardRequestBody): ValidationResult {
    const errors: Record<string, string> = {};

    if (body.credentialTtl === "other") {
      const expiryErrors = validateCredentialExpiryDate(
        body["credentialExpiry-day"],
        body["credentialExpiry-month"],
        body["credentialExpiry-year"],
      );
      Object.assign(errors, expiryErrors);
    }

    return {
      isValid: Object.keys(errors).length === 0,
      errors,
    };
  }
}
