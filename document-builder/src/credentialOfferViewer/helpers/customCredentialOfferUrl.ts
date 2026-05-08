import { CredentialOffer } from "../types/CredentialOfferResponse";
import { WalletAppsConfig } from "../../config/walletAppsConfig";

const WALLET_URL_PATH_SPLITTER = "account.gov.uk/wallet/";
const CREDENTIAL_OFFER_SPLITTER = "credential_offer=";

function isInvalidUrl(urlParts: string[]) {
  return urlParts.length !== 3;
}

function replacePath(credentialOfferUrl: string, newPath: string) {
  const urlParts = credentialOfferUrl.split(
    new RegExp(`(${WALLET_URL_PATH_SPLITTER})`),
  );
  if (isInvalidUrl(urlParts)) {
    throw new Error("Invalid URL");
  }
  return newPath + urlParts[2];
}

function replacePreAuthorizedCodeWithError(
  credentialOfferUrl: string,
  errorScenario: string,
) {
  const decodedUrl = decodeURIComponent(credentialOfferUrl);
  const urlParts = decodedUrl.split(
    new RegExp(`(${CREDENTIAL_OFFER_SPLITTER})`),
  );
  if (isInvalidUrl(urlParts)) {
    throw new Error("Invalid URL");
  }

  const credentialOfferString = urlParts[2];
  const credentialOffer: CredentialOffer = JSON.parse(credentialOfferString);

  // replace credential offer pre-authorized_code with error string
  credentialOffer.grants[
    "urn:ietf:params:oauth:grant-type:pre-authorized_code"
  ]["pre-authorized_code"] = errorScenario;
  const newCredentialOfferEncoded = encodeURIComponent(
    JSON.stringify(credentialOffer),
  );

  return urlParts[0] + urlParts[1] + newCredentialOfferEncoded;
}

export function customiseCredentialOfferUrl(
  credentialOfferUrl: string,
  selectedApp: string,
  walletAppsConfig: WalletAppsConfig,
  walletApps: string[],
  errorScenario: string | undefined,
) {
  if (!walletApps.includes(selectedApp)) {
    throw new Error("Unknown app selected");
  }

  const newCredentialOfferUrl = replacePath(
    credentialOfferUrl,
    walletAppsConfig[selectedApp].url,
  );

  if (errorScenario) {
    return replacePreAuthorizedCodeWithError(
      newCredentialOfferUrl,
      errorScenario,
    );
  } else {
    return newCredentialOfferUrl;
  }
}
