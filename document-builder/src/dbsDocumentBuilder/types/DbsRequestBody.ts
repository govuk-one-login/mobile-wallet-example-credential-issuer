export interface DbsRequestBody {
  "issuance-day": string;
  "issuance-month": string;
  "issuance-year": string;
  "expiration-day": string;
  "expiration-month": string;
  "expiration-year": string;
  "birth-day": string;
  "birth-month": string;
  "birth-year": string;
  firstName: string;
  lastName: string;
  subBuildingName: string;
  buildingName: string;
  streetName: string;
  addressLocality: string;
  addressCountry: string;
  postalCode: string;
  certificateNumber: string;
  applicationNumber: string;
  credentialTtl: string;
  throwError: string;
}
