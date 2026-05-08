import { Request, Response } from "express";
import { saveDocument } from "../services/databaseService";
import { CredentialType } from "../types/CredentialType";
import { logger } from "../middleware/logger";
import { isAuthenticated } from "../utils/isAuthenticated";
import {
  getDocumentsTableName,
  getEnvironment,
  getPhotosBucketName,
  getTableItemTtl,
} from "../config/appConfig";
import { VeteranCardData } from "./types/VeteranCardData";
import { VeteranCardRequestBody } from "./types/VeteranCardRequestBody";
import { ERROR_CHOICES } from "../utils/errorChoices";
import { getTimeToLiveEpoch } from "../utils/getTimeToLiveEpoch";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";
import { getViewCredentialOfferRedirectUrl } from "../utils/getViewCredentialOfferRedirectUrl";
import { randomUUID } from "node:crypto";
import { getPhoto } from "../utils/photoUtils";
import { uploadPhoto } from "../services/s3Service";

const CREDENTIAL_TYPE = CredentialType.DigitalVeteranCard;

export interface VeteranCardDocumentBuilderControllerConfig {
  environment?: string;
}

export function veteranCardDocumentBuilderGetController({
  environment = getEnvironment(),
}: VeteranCardDocumentBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (req: Request, res: Response): Promise<void> {
    try {
      const showThrowError = environment !== "staging";
      res.render("veteran-card-document-details-form.njk", {
        authenticated: isAuthenticated(req),
        errorChoices: ERROR_CHOICES,
        showThrowError,
      });
    } catch (error) {
      logger.error(
        error,
        "An error happened rendering Veteran Card document page",
      );
      res.render("500.njk");
    }
  };
}

export async function veteranCardDocumentBuilderPostController(
  req: Request,
  res: Response,
): Promise<void> {
  try {
    const body: VeteranCardRequestBody = req.body;
    const { photoBuffer, mimeType } = getPhoto(body.portrait);

    const bucketName = getPhotosBucketName();
    const itemId = randomUUID();
    await uploadPhoto(photoBuffer, itemId, bucketName, mimeType);

    const s3Uri = `s3://${bucketName}/${itemId}`;
    const data = buildVeteranCardDataFromRequestBody(body, s3Uri);
    await saveDocument(getDocumentsTableName(), {
      itemId,
      documentId: data.serviceNumber,
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
    logger.error(
      error,
      "An error happened processing Veteran Card document request",
    );
    res.render("500.njk");
  }
}

function buildVeteranCardDataFromRequestBody(
  body: VeteranCardRequestBody,
  s3Uri: string,
) {
  const {
    throwError: _throwError,
    credentialTtl: _credentialTtl,
    ...newObject
  } = body;

  const data: VeteranCardData = {
    ...newObject,
    portrait: s3Uri,
  };

  return data;
}
