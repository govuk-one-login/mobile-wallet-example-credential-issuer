import { Request, Response } from "express";
import QRCode from "qrcode";
import { getCredentialOfferUrl } from "./services/credentialOfferService";
import { customiseCredentialOfferUrl } from "./helpers/customCredentialOfferUrl";
import { logger } from "../middleware/logger";
import { isAuthenticated } from "../utils/isAuthenticated";
import { getEnvironment, getWalletApps } from "../config/appConfig";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";
import {
  WalletAppsConfig,
  walletAppsConfig as config,
} from "../config/walletAppsConfig";

export interface CredentialOfferViewerConfig {
  walletAppsConfig?: WalletAppsConfig;
  walletApps?: string[];
  environment?: string;
}

export function credentialOfferViewerController({
  walletAppsConfig = config,
  walletApps = getWalletApps(),
  environment = getEnvironment(),
}: CredentialOfferViewerConfig = {}): ExpressRouteFunction {
  return async function (req: Request, res: Response): Promise<void> {
    try {
      const itemId = req.params.itemId as string;
      const selectedApp = req.cookies.app;
      const credentialType = req.query.type as string;
      const errorScenario = req.query.error as string;
      const walletSubjectId = req.cookies.wallet_subject_id;

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

      return res.render("credential-offer.njk", {
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
