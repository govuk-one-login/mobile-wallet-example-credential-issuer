import { Request, Response } from "express";
import { randomUUID } from "node:crypto";
import { saveDocument } from "../services/databaseService";
import { CredentialType } from "../types/CredentialType";
import { logger } from "../middleware/logger";
import { isAuthenticated } from "../utils/isAuthenticated";
import {
  getDocumentsTableName,
  getEnvironment,
  getTableItemTtl,
} from "../config/appConfig";
import { NinoRequestBody } from "./types/NinoRequestBody";
import { NinoData } from "./types/NinoData";
import { ERROR_CHOICES } from "../utils/errorChoices";
import { getTimeToLiveEpoch } from "../utils/getTimeToLiveEpoch";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";
import { getViewCredentialOfferRedirectUrl } from "../utils/getViewCredentialOfferRedirectUrl";

const CREDENTIAL_TYPE = CredentialType.SocialSecurityCredential;

export interface NinoDocumentBuilderControllerConfig {
  environment?: string;
}

export function ninoDocumentBuilderGetController({
  environment = getEnvironment(),
}: NinoDocumentBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (req: Request, res: Response): Promise<void> {
    try {
      const showThrowError = environment !== "staging";
      res.render("nino-document-details-form.njk", {
        authenticated: isAuthenticated(req),
        errorChoices: ERROR_CHOICES,
        showThrowError,
      });
    } catch (error) {
      logger.error(error, "An error happened rendering NINO document page");
      res.render("500.njk");
    }
  };
}

export async function ninoDocumentBuilderPostController(
  req: Request,
  res: Response,
): Promise<void> {
  try {
    const body: NinoRequestBody = req.body;
    const data = buildNinoDataFromRequestBody(body);
    const itemId = randomUUID();
    await saveDocument(getDocumentsTableName(), {
      itemId,
      documentId: data.nino,
      data,
      vcType: CREDENTIAL_TYPE,
      credentialTtlSeconds: Number(body.credentialTtl),
      timeToLive: getTimeToLiveEpoch(getTableItemTtl()),
    });

    const redirectUrl = getViewCredentialOfferRedirectUrl({
      itemId,
      credentialType: CREDENTIAL_TYPE,
      selectedError: body["throwError"],
    });
    res.redirect(redirectUrl);
  } catch (error) {
    logger.error(error, "An error happened processing NINO document request");
    res.render("500.njk");
  }
}

function buildNinoDataFromRequestBody(body: NinoRequestBody) {
  const {
    throwError: _throwError,
    credentialTtl: _credentialTtl,
    ...newObject
  } = body;
  const data: NinoData = {
    ...newObject,
  };
  return data;
}
