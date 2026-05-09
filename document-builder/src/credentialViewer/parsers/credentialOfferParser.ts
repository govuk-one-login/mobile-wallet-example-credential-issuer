/**
 * Extracts pre-authorized code from credential offer URI
 * @param credentialOfferUri - URI containing the credential offer
 * @returns Pre-authorized code string
 */
export function extractPreAuthCode(credentialOfferUri: string): string {
  const credentialOfferString = credentialOfferUri.split(
    "add?credential_offer=",
  )[1];
  const credentialOffer = JSON.parse(credentialOfferString);
  return credentialOffer.grants[
    "urn:ietf:params:oauth:grant-type:pre-authorized_code"
  ]["pre-authorized_code"];
}
