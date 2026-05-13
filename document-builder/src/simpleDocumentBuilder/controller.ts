import { Request, Response } from "express";
import { formatDate, getDefaultDates, validateDateFields } from "../utils/date";
import { isAuthenticated } from "../utils/isAuthenticated";
import { ERROR_CHOICES } from "../utils/errorChoices";
import { logger } from "../middleware/logger";
import { randomUUID } from "node:crypto";
import {
  getDocumentsTableName,
  getEnvironment,
  getPhotosBucketName,
  getTableItemTtl,
} from "../config/appConfig";
import { getTimeToLiveEpoch } from "../utils/getTimeToLiveEpoch";
import { SimpleDocumentRequestBody } from "./types/SimpleDocumentRequestBody";
import { saveDocument } from "../services/databaseService";
import { CredentialType } from "../types/CredentialType";
import { SimpleDocumentData } from "./types/SimpleDocumentData";
import { getRandomIntInclusive } from "../utils/getRandomIntInclusive";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";
import { getViewCredentialOfferRedirectUrl } from "../utils/getViewCredentialOfferRedirectUrl";
import { getPhoto } from "../utils/photoUtils";
import { uploadPhoto } from "../services/s3Service";

const CREDENTIAL_TYPE = CredentialType.SimpleDocument;
const FISH_TYPES = [
  "Coarse fish",
  "Salmon and trout",
  "Sea fishing",
  "All freshwater fish",
];
const FISH_TYPE_UI_OPTIONS = FISH_TYPES.map((type, index) => ({
  value: type,
  text: type,
  selected: index === 0,
}));

export interface SimpleDocumentBuilderControllerConfig {
  environment?: string;
}

export function simpleDocumentBuilderGetController({
  environment = getEnvironment(),
}: SimpleDocumentBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (req: Request, res: Response): Promise<void> {
    try {
      const { defaultIssueDate, defaultExpiryDate } = getDefaultDates();
      const documentNumber = "FLN" + getRandomIntInclusive();
      const showThrowError = environment !== "staging";
      res.render("simple-document-details-form.njk", {
        defaultIssueDate,
        defaultExpiryDate,
        documentNumber,
        fishTypeOptions: FISH_TYPE_UI_OPTIONS,
        authenticated: isAuthenticated(req),
        errorChoices: ERROR_CHOICES,
        showThrowError,
      });
    } catch (error) {
      logger.error(
        error,
        "An error happened rendering the Simple Document page",
      );
      res.render("500.njk");
    }
  };
}

export function simpleDocumentBuilderPostController({
  environment = getEnvironment(),
}: SimpleDocumentBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (req: Request, res: Response): Promise<void> {
    try {
      const body: SimpleDocumentRequestBody = req.body;

      const errors = validateDateFields(body);
      if (!FISH_TYPES.includes(body.type_of_fish)) {
        errors.type_of_fish = "Select a valid type of fish";
      }
      if (Object.keys(errors).length > 0) {
        const { defaultIssueDate, defaultExpiryDate } = getDefaultDates();
        const documentNumber = body.document_number;
        const showThrowError = environment !== "staging";
        return res.render("simple-document-details-form.njk", {
          defaultIssueDate,
          defaultExpiryDate,
          documentNumber,
          fishTypeOptions: FISH_TYPE_UI_OPTIONS,
          authenticated: isAuthenticated(req),
          errorChoices: ERROR_CHOICES,
          showThrowError,
          errors,
        });
      }

      const bucketName = getPhotosBucketName();
      const itemId = randomUUID();
      const s3Uri = `s3://${bucketName}/${itemId}`;

      const { photoBuffer, mimeType } = getPhoto(body.portrait);
      await uploadPhoto(photoBuffer, itemId, bucketName, mimeType);

      const data = buildSimpleDocumentDataFromRequestBody(body, s3Uri);
      await saveDocument(getDocumentsTableName(), {
        itemId,
        documentId: data.document_number,
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
        "An error happened processing the Simple Document request",
      );
      res.render("500.njk");
    }
  };
}

function buildSimpleDocumentDataFromRequestBody(
  body: SimpleDocumentRequestBody,
  s3Uri: string,
): SimpleDocumentData {
  const birthDay = body["birth-day"];
  const birthMonth = body["birth-month"];
  const birthYear = body["birth-year"];
  const issueDay = body["issue-day"];
  const issueMonth = body["issue-month"];
  const issueYear = body["issue-year"];
  const expiryDay = body["expiry-day"];
  const expiryMonth = body["expiry-month"];
  const expiryYear = body["expiry-year"];

  return {
    family_name: body.family_name,
    given_name: body.given_name,
    portrait: s3Uri,
    birth_date: formatDate(birthDay, birthMonth, birthYear),
    issue_date: formatDate(issueDay, issueMonth, issueYear),
    expiry_date: formatDate(expiryDay, expiryMonth, expiryYear),
    issuing_country: body.issuing_country,
    document_number: body.document_number,
    type_of_fish: body.type_of_fish,
    number_of_fishing_rods: body.number_of_fishing_rods,
  };
}
