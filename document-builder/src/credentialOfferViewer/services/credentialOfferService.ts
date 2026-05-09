import axios from "axios";
import { getCriEndpoint } from "../../config/appConfig";
import { logger } from "../../middleware/logger";

const CREDENTIAL_OFFER_PATH = "/credential_offer";

export async function getCredentialOfferUrl(
  walletSubjectId: string,
  itemId: string,
  credentialType: string,
): Promise<string> {
  const criUrl = getCriEndpoint();
  const credentialOfferUrl = criUrl + CREDENTIAL_OFFER_PATH;

  const response = await axios.get(credentialOfferUrl, {
    params: {
      walletSubjectId: walletSubjectId,
      itemId: itemId,
      credentialType: credentialType,
    },
  });

  logger.info(`Fetched credential offer for item with ID ${itemId}`);

  return response.data;
}
