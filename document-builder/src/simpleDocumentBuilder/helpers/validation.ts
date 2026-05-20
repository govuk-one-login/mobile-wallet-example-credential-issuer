import {
  validateBirthDate,
  validateIssueDate,
  validateExpiryDate,
} from "../../utils/date";
import { SimpleDocumentRequestBody } from "../types/SimpleDocumentRequestBody";

export function validateSimpleDocumentForm(
  body: SimpleDocumentRequestBody,
): Record<string, string> {
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

  return { ...birthErrors, ...issueErrors, ...expiryErrors };
}
