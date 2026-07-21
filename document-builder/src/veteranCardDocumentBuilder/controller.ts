import { NextFunction, Request, Response } from "express";
import { saveDocument } from "../services/databaseService";
import { CredentialType } from "../types/CredentialType";
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
import { calculateCredentialTtlSeconds } from "../utils/calculateCredentialTtlSeconds";
import { validateVeteranCardForm } from "./helpers/VeteranCardFormValidator";
import {
  CUSTOM_CREDENTIAL_TTL,
  SECONDS_IN_A_DAY,
} from "../config/credentialTtl";
import { ENVIRONMENTS } from "../config/environments";

const CREDENTIAL_TYPE = CredentialType.DigitalVeteranCard;

export interface VeteranCardDocumentBuilderControllerConfig {
  environment?: string;
}

export function veteranCardDocumentBuilderGetController({
  environment = getEnvironment(),
}: VeteranCardDocumentBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> {
    try {
      res.render("veteran-card-document-details-form.njk", {
        authenticated: isAuthenticated(req),
        errorChoices: ERROR_CHOICES,
        showThrowError: environment !== ENVIRONMENTS.STAGE,
      });
    } catch (error) {
      next(
        new Error("An error happened rendering Veteran Card document page", {
          cause: error,
        }),
      );
    }
  };
}

export function veteranCardDocumentBuilderPostController({
  environment = getEnvironment(),
}: VeteranCardDocumentBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> {
    try {
      const body: VeteranCardRequestBody = req.body;

      const result = validateVeteranCardForm(body);
      if (!result.isValid) {
        return res.render("veteran-card-document-details-form.njk", {
          authenticated: isAuthenticated(req),
          errorChoices: ERROR_CHOICES,
          showThrowError: environment !== ENVIRONMENTS.STAGE,
          errors: result.errors,
          credentialTtl: body.credentialTtl,
        });
      }

      const itemId = randomUUID();

      const bucketName = getPhotosBucketName();
      const s3Uri = `s3://${bucketName}/${itemId}`;
      const { photoBuffer, mimeType } = getPhoto(body.portrait);
      await uploadPhoto(photoBuffer, itemId, bucketName, mimeType);

      const data = buildVeteranCardDataFromRequestBody(body, s3Uri);
      const credentialTtlSeconds =
        body.credentialTtl === CUSTOM_CREDENTIAL_TTL
          ? calculateCredentialTtlSeconds(
              body["credentialExpiry-day"],
              body["credentialExpiry-month"],
              body["credentialExpiry-year"],
            )
          : Number(body.credentialTtl);
      const expectedUpdateSeconds = body.expectedUpdateDays
        ? credentialTtlSeconds -
          Number(body.expectedUpdateDays) * SECONDS_IN_A_DAY
        : null;
      await saveDocument(getDocumentsTableName(), {
        itemId,
        documentId: data.serviceNumber,
        data,
        vcType: CREDENTIAL_TYPE,
        credentialTtlSeconds,
        expectedUpdateSeconds,
        timeToLive: getTimeToLiveEpoch(getTableItemTtl()),
      });

      const redirectUrl = getViewCredentialOfferRedirectUrl({
        itemId,
        credentialType: CREDENTIAL_TYPE,
        selectedError: body["throwError"],
      });
      res.redirect(redirectUrl);
    } catch (error) {
      next(
        new Error(
          "An error happened processing Veteran Card document request",
          { cause: error },
        ),
      );
    }
  };
}

function buildVeteranCardDataFromRequestBody(
  body: VeteranCardRequestBody,
  s3Uri: string,
) {
  const {
    throwError: _throwError,
    credentialTtl: _credentialTtl,
    expectedUpdateDays: _expectedUpdateDays,
    "credentialExpiry-day": _credentialExpiryDay,
    "credentialExpiry-month": _credentialExpiryMonth,
    "credentialExpiry-year": _credentialExpiryYear,
    ...newObject
  } = body;

  const data: VeteranCardData = {
    ...newObject,
    portrait: s3Uri,
  };

  return data;
}
