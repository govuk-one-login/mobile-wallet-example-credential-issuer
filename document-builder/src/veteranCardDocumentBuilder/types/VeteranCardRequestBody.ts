export interface VeteranCardRequestBody {
  givenName: string;
  familyName: string;
  "dateOfBirth-day": string;
  "dateOfBirth-month": string;
  "dateOfBirth-year": string;
  "cardExpiryDate-day": string;
  "cardExpiryDate-month": string;
  "cardExpiryDate-year": string;
  serviceNumber: string;
  serviceBranch: string;
  portrait: string;
  credentialTtl: string;
  throwError: string;
}
