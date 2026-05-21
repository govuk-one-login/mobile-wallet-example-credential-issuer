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

    const birthErrors = validateBirthDate(
      body["birth-day"],
      body["birth-month"],
      body["birth-year"],
    );
    const issueErrors = validateIssueDate(
      body["issue-day"],
      body["issue-month"],
      body["issue-year"],
    );
    const expiryErrors = validateExpiryDate(
      body["expiry-day"],
      body["expiry-month"],
      body["expiry-year"],
    );

    Object.assign(errors, birthErrors, issueErrors, expiryErrors);

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
