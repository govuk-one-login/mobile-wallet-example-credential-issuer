import { Request, Response } from "express";
import QRCode from "qrcode";
import { logger } from "../middleware/logger";
import { isAuthenticated } from "../utils/isAuthenticated";
import {
  getEnvironment,
  getHardcodedWalletSubjectId,
  getWalletApps,
} from "../config/appConfig";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";
import {
  WalletAppsConfig,
  WALLET_APPS,
  walletAppsConfig as config,
} from "../config/walletAppsConfig";
import {
  dvsRoutesNonProdEnvs,
  dvsRoutesProdEnvs,
} from "../config/environments";
import { CredentialType } from "../types/CredentialType";
import { getCredentialOfferUrl } from "../credentialOfferViewer/services/credentialOfferService";
import { customiseCredentialOfferUrl } from "../credentialOfferViewer/helpers/customCredentialOfferUrl";
import { ROUTES } from "../config/routes";

export interface CredentialOfferViewerConfig {
  walletAppsConfig?: WalletAppsConfig;
  walletApps?: string[];
  environment?: string;
}

export function dvsCredentialOfferViewerController({
  walletAppsConfig = config,
  walletApps = getWalletApps(),
  environment = getEnvironment(),
}: CredentialOfferViewerConfig = {}): ExpressRouteFunction {
  return async function (req: Request, res: Response): Promise<void> {
    try {
      const errorScenario = "";
      const itemId = req.params.itemId as string;
      const walletSubjectId = getHardcodedWalletSubjectId();
      const credentialType = CredentialType.MobileDrivingLicence;
      let selectedApp = "";

      if (dvsRoutesNonProdEnvs.includes(environment)) {
        selectedApp = WALLET_APPS.WALLET_TEST_BUILD;
      } else if (dvsRoutesProdEnvs.includes(environment)) {
        selectedApp = WALLET_APPS.WALLET_TEST_VERIFIER_INTEGRATION;
      } else {
        return res.redirect(ROUTES.START);
      }

      const credentialOfferUrl = await getCredentialOfferUrl(
        walletSubjectId,
        itemId,
        credentialType,
      );

      const customisedCredentialOfferUrl = customiseCredentialOfferUrl(
        credentialOfferUrl,
        selectedApp,
        walletAppsConfig,
        walletApps,
        errorScenario,
      );

      const qrCode = await QRCode.toDataURL(customisedCredentialOfferUrl);

      return res.render("dvs-credential-offer.njk", {
        authenticated: isAuthenticated(req),
        universalLink: customisedCredentialOfferUrl,
        qrCode,
        environment,
      });
    } catch (error) {
      logger.error(
        error,
        "An error happened processing credential offer request",
      );
      return res.render("500.njk");
    }
  };
}
