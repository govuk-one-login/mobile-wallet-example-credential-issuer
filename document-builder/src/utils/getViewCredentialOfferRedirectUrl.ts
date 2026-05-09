import { isErrorCode } from "./isErrorCode";
import { ROUTES } from "../config/routes";

interface GetViewCredentialOfferRedirectUrlProps {
  isDvsRoute?: boolean;
  itemId: string;
  credentialType: string;
  selectedError?: string;
}

export function getViewCredentialOfferRedirectUrl({
  isDvsRoute,
  itemId,
  credentialType,
  selectedError,
}: GetViewCredentialOfferRedirectUrlProps): string {
  const viewCredentialOfferBase = isDvsRoute
    ? ROUTES.DVS_CREDENTIAL_OFFER_VIEWER.replace("/:itemId", "")
    : ROUTES.CREDENTIAL_OFFER_VIEWER.replace("/:itemId", "");
  let redirectUrl = `${viewCredentialOfferBase}/${itemId}?type=${credentialType}`;
  if (selectedError && isErrorCode(selectedError)) {
    redirectUrl += `&error=${selectedError}`;
  }
  return redirectUrl;
}
