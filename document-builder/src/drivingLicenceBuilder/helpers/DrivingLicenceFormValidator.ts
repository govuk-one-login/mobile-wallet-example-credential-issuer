import { DrivingLicenceRequestBody } from "../types/DrivingLicenceRequestBody";
import {
  validateBirthDate,
  validateIssueDate,
  validateExpiryDate,
  validateCredentialExpiryDate,
} from "../../utils/date";
import { ValidationResult } from "../../types/ValidationResult";

export class DrivingLicenceFormValidator {
  validate(body: DrivingLicenceRequestBody): ValidationResult {
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

    if (body.credentialTtl === "other") {
      const credentialExpiryErrors = validateCredentialExpiryDate(
        body["credentialExpiry-day"],
        body["credentialExpiry-month"],
        body["credentialExpiry-year"],
      );
      Object.assign(errors, credentialExpiryErrors);
    }

    return {
      isValid: Object.keys(errors).length === 0,
      errors,
    };
  }
}
