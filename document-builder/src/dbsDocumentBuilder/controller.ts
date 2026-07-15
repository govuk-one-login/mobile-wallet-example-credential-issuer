import { NextFunction, Request, Response } from "express";
import { randomUUID } from "node:crypto";
import { saveDocument } from "../services/databaseService";
import { CredentialType } from "../types/CredentialType";
import { isAuthenticated } from "../utils/isAuthenticated";
import {
  getDocumentsTableName,
  getEnvironment,
  getTableItemTtl,
} from "../config/appConfig";
import { DbsRequestBody } from "./types/DbsRequestBody";
import { DbsData } from "./types/DbsData";
import { ERROR_CHOICES } from "../utils/errorChoices";
import { getTimeToLiveEpoch } from "../utils/getTimeToLiveEpoch";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";
import { getViewCredentialOfferRedirectUrl } from "../utils/getViewCredentialOfferRedirectUrl";
import { ENVIRONMENTS } from "../config/environments";

const CREDENTIAL_TYPE = CredentialType.BasicDisclosureCredential;

export interface DbsDocumentBuilderControllerConfig {
  environment?: string;
}

export function dbsDocumentBuilderGetController({
  environment = getEnvironment(),
}: DbsDocumentBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (req: Request, res: Response, next: NextFunction): Promise<void> {
    try {
      const showThrowError = environment !== ENVIRONMENTS.STAGE;
      res.render("dbs-document-details-form.njk", {
        authenticated: isAuthenticated(req),
        errorChoices: ERROR_CHOICES,
        showThrowError,
      });
    } catch (error) {
      next(
        new Error("An error happened rendering DBS document page", {
          cause: error,
        }),
      );
    }
  };
}

export async function dbsDocumentBuilderPostController(
  req: Request,
  res: Response,
  next: NextFunction,
): Promise<void> {
  try {
    const body: DbsRequestBody = req.body;
    const data = buildDbsDataFromRequestBody(body);
    const timeToLive = getTimeToLiveEpoch(getTableItemTtl());
    const itemId = randomUUID();
    await saveDocument(getDocumentsTableName(), {
      itemId,
      documentId: data.certificateNumber,
      data,
      vcType: CREDENTIAL_TYPE,
      credentialTtlSeconds: Number(body.credentialTtl),
      expectedUpdateSeconds: null,
      timeToLive,
    });

    const redirectUrl = getViewCredentialOfferRedirectUrl({
      itemId,
      credentialType: CREDENTIAL_TYPE,
      selectedError: body["throwError"],
    });
    res.redirect(redirectUrl);
  } catch (error) {
    next(
      new Error("An error happened processing DBS document request", {
        cause: error,
      }),
    );
  }
}

function buildDbsDataFromRequestBody(body: DbsRequestBody) {
  const {
    throwError: _throwError,
    credentialTtl: _credentialTtl,
    ...newObject
  } = body;
  const data: DbsData = {
    certificateType: "basic",
    outcome: "Result clear",
    policeRecordsCheck: "Clear",
    ...newObject,
  };
  return data;
}
