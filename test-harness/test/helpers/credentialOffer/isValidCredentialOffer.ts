import { credentialOfferSchema } from "./credentialOfferSchema";
import { getAjvInstance } from "../ajv/ajvInstance";

export interface CredentialOffer {
  credential_issuer: string;
  credential_configuration_ids: string[];
  grants: Grants;
}

interface Grants {
  "urn:ietf:params:oauth:grant-type:pre-authorized_code": PreAuthorizedCode;
}

interface PreAuthorizedCode {
  "pre-authorized_code": string;
}

export function isValidCredentialOffer(credentialOfferDeepLink: string) {
  const credentialOfferString = extractCredentialOffer(credentialOfferDeepLink);
  const credentialOffer: CredentialOffer = parseAsJson(credentialOfferString);

  const ajv = getAjvInstance();
  const rulesValidator = ajv.compile(credentialOfferSchema);
  if (!rulesValidator(credentialOffer)) {
    throw new Error(
      `INVALID_CREDENTIAL_OFFER: ${JSON.stringify(rulesValidator.errors)}`,
    );
  }
  return true;
}

export function parseAsJson(inputString: string) {
  try {
    return JSON.parse(inputString);
  } catch (error) {
    throw new Error(
      `INVALID_CREDENTIAL_OFFER: Not a valid JSON. ${JSON.stringify(error)}`,
    );
  }
}

export function extractCredentialOffer(urlString: string) {
  let url: URL;
  try {
    url = new URL(urlString);
  } catch (error) {
    throw new Error(`INVALID_DEEP_LINK: ${JSON.stringify(error)}`);
  }
  const credentialOffer = url.searchParams.get("credential_offer");
  if (credentialOffer == null) {
    throw new Error("INVALID_DEEP_LINK: Missing 'credential_offer'");
  } else {
    return credentialOffer;
  }
}
