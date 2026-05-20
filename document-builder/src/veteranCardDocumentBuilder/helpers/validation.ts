import { VeteranCardRequestBody } from "../types/VeteranCardRequestBody";
import { validateCredentialExpiryDate } from "../../utils/date";

export function validateVeteranCardForm(
  body: VeteranCardRequestBody,
): Record<string, string> {
  return validateCredentialExpiryDate(
    body["credentialExpiry-day"],
    body["credentialExpiry-month"],
    body["credentialExpiry-year"],
  );
}
