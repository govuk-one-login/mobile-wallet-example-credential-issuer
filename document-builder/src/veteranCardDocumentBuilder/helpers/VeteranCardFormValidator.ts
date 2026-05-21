import { VeteranCardRequestBody } from "../types/VeteranCardRequestBody";
import { validateCredentialExpiryDate } from "../../utils/date";

export interface ValidationResult {
  isValid: boolean;
  errors: Record<string, string>;
}

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
