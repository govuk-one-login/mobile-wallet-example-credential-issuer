import { SimpleDocumentRequestBody } from "../types/SimpleDocumentRequestBody";
import {
  validateBirthDate,
  validateIssueDate,
  validateExpiryDate,
} from "../../utils/date";
import { ValidationResult } from "../../types/ValidationResult";

const FISH_TYPES = new Set<string>([
  "Coarse fish",
  "Salmon and trout",
  "Sea fishing",
  "All freshwater fish",
]);

export class SimpleDocumentFormValidator {
  validate(body: SimpleDocumentRequestBody): ValidationResult {
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

    if (!FISH_TYPES.has(body.type_of_fish)) {
      errors.type_of_fish = "Select a valid type of fish";
    }

    return {
      isValid: Object.keys(errors).length === 0,
      errors,
    };
  }
}
