export interface CredentialOffer {
  credentials: string[];
  grants: Grants;
  credentialIssuer: string;
}

export interface Grants {
  "urn:ietf:params:oauth:grant-type:pre-authorized_code": PreAuthorizedCode;
}

export interface PreAuthorizedCode {
  "pre-authorized_code": string;
}
