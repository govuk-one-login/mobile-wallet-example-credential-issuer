import { SimpleDocumentRequestBody } from "../types/SimpleDocumentRequestBody";
import {
  validateBirthDate,
  validateIssueDate,
  validateExpiryDate,
} from "../../utils/date";

export interface ValidationResult {
  isValid: boolean;
  errors: Record<string, string>;
}

const FISH_TYPES = [
  "Coarse fish",
  "Salmon and trout",
  "Sea fishing",
  "All freshwater fish",
];

export class SimpleDocumentFormValidator {
  validate(body: SimpleDocumentRequestBody): ValidationResult {
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

    if (!FISH_TYPES.includes(body.type_of_fish)) {
      errors.type_of_fish = "Select a valid type of fish";
    }

    return {
      isValid: Object.keys(errors).length === 0,
      errors,
    };
  }
}
