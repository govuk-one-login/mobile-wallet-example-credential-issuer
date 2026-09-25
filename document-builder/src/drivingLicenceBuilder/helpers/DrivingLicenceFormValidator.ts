import { DrivingLicenceRequestBody } from "../types/DrivingLicenceRequestBody";
import {
  validateBirthDate,
  validateIssueDate,
  validateExpiryDate,
  validateCredentialExpiryDate,
} from "../../utils/date";
import { ValidationResult } from "../../types/ValidationResult";
import { CUSTOM_CREDENTIAL_TTL } from "../../config/credentialTtl";

export const ALLOWED_ISSUING_AUTHORITIES = ["DVLA", "GDS"] as const;

export function validateDrivingLicenceForm(
  body: DrivingLicenceRequestBody,
): ValidationResult {
  const errors: Record<string, string> = {};

  const birthDateErrors = validateBirthDate(
    body["birth-day"],
    body["birth-month"],
    body["birth-year"],
  );
  const issueDateErrors = validateIssueDate(
    body["issue-day"],
    body["issue-month"],
    body["issue-year"],
  );
  const expiryDateErrors = validateExpiryDate(
    body["expiry-day"],
    body["expiry-month"],
    body["expiry-year"],
  );

  Object.assign(errors, birthDateErrors, issueDateErrors, expiryDateErrors);

  if (body.credentialTtl === CUSTOM_CREDENTIAL_TTL) {
    const credentialExpiryErrors = validateCredentialExpiryDate(
      body["credentialExpiry-day"],
      body["credentialExpiry-month"],
      body["credentialExpiry-year"],
    );
    Object.assign(errors, credentialExpiryErrors);
  }

  if (
    body.expectedUpdateDays &&
    Number.isNaN(Number(body.expectedUpdateDays))
  ) {
    errors.expected_update = "Enter a number";
  }

  if (
    !ALLOWED_ISSUING_AUTHORITIES.includes(
      body.issuing_authority as (typeof ALLOWED_ISSUING_AUTHORITIES)[number],
    )
  ) {
    errors.issuing_authority = `Issuing authority must be one of: ${ALLOWED_ISSUING_AUTHORITIES.join(", ")}`;
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors,
  };
}
