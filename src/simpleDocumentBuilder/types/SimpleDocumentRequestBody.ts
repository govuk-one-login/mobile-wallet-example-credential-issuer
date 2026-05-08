export interface SimpleDocumentRequestBody {
  family_name: string;
  given_name: string;
  portrait: string;
  "birth-day": string;
  "birth-month": string;
  "birth-year": string;
  "issue-day": string;
  "issue-month": string;
  "issue-year": string;
  "expiry-day": string;
  "expiry-month": string;
  "expiry-year": string;
  issuing_country: string;
  document_number: string;
  type_of_fish: string;
  number_of_fishing_rods: string;
  credentialTtl: string;
  throwError: string;
}
