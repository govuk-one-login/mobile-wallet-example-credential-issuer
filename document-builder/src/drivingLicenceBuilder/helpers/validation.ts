import {
  validateBirthDate,
  validateIssueDate,
  validateExpiryDate,
  validateCredentialExpiryDate,
} from "../../utils/date";
import { DrivingLicenceRequestBody } from "../types/DrivingLicenceRequestBody";

export function validateDrivingLicenceForm(
  body: DrivingLicenceRequestBody,
): Record<string, string> {
  const birthDateError = validateBirthDate(
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
  const credentialExpiryDateErrors = validateCredentialExpiryDate(
    body["credentialExpiry-day"],
    body["credentialExpiry-month"],
    body["credentialExpiry-year"],
  );

  return {
    ...birthDateError,
    ...issueDateErrors,
    ...expiryDateErrors,
    ...credentialExpiryDateErrors,
  };
}
