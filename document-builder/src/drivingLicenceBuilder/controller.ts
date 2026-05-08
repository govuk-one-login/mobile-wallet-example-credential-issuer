import { Request, Response } from "express";
import {
  getDocumentsTableName,
  getEnvironment,
  getPhotosBucketName,
  getTableItemTtl,
} from "../config/appConfig";
import { CredentialType } from "../types/CredentialType";
import { isAuthenticated } from "../utils/isAuthenticated";
import { logger } from "../middleware/logger";
import { randomUUID } from "node:crypto";
import { saveDocument } from "../services/databaseService";
import { validateDateFields, getDefaultDates, formatDate } from "../utils/date";
import {
  getFullDrivingPrivileges,
  getProvisionalDrivingPrivileges,
} from "./helpers/drivingPrivilegeBuilder";
import { ERROR_CHOICES } from "../utils/errorChoices";
import { getTimeToLiveEpoch } from "../utils/getTimeToLiveEpoch";
import { getRandomIntInclusive } from "../utils/getRandomIntInclusive";
import { ExpressRouteFunction } from "../types/ExpressRouteFunction";
import { getViewCredentialOfferRedirectUrl } from "../utils/getViewCredentialOfferRedirectUrl";
import { DrivingLicenceRequestBody } from "./types/DrivingLicenceRequestBody";
import { DrivingLicenceData } from "../types/DrivingLicenceData";
import { uploadPhoto } from "../services/s3Service";
import { getPhoto } from "../utils/photoUtils";

const CREDENTIAL_TYPE = CredentialType.MobileDrivingLicence;

export interface DrivingLicenceBuilderControllerConfig {
  environment?: string;
}

export function drivingLicenceBuilderGetController({
  environment = getEnvironment(),
}: DrivingLicenceBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (req: Request, res: Response): Promise<void> {
    try {
      const showThrowError = environment !== "staging";
      const { defaultIssueDate, defaultExpiryDate } = getDefaultDates();
      const drivingLicenceNumber = "EDWAR" + getRandomIntInclusive() + "SE5RO";
      res.render("driving-licence-form.njk", {
        defaultIssueDate,
        defaultExpiryDate,
        drivingLicenceNumber,
        authenticated: isAuthenticated(req),
        errorChoices: ERROR_CHOICES,
        showThrowError,
      });
    } catch (error) {
      logger.error(
        error,
        "An error happened rendering Driving Licence document page",
      );
      res.render("500.njk");
    }
  };
}

export function drivingLicenceBuilderPostController({
  environment = getEnvironment(),
}: DrivingLicenceBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (req: Request, res: Response): Promise<void> {
    try {
      const body: DrivingLicenceRequestBody = req.body;

      const errors = validateDateFields(body);
      if (Object.keys(errors).length > 0) {
        const { defaultIssueDate, defaultExpiryDate } = getDefaultDates();
        const drivingLicenceNumber = body.document_number;
        const showThrowError = environment !== "staging";
        return res.render("driving-licence-form.njk", {
          defaultIssueDate,
          defaultExpiryDate,
          drivingLicenceNumber,
          authenticated: isAuthenticated(req),
          errorChoices: ERROR_CHOICES,
          showThrowError,
          errors,
        });
      }

      const itemId = randomUUID();
      const bucketName = getPhotosBucketName();
      const s3Uri = `s3://${bucketName}/${itemId}`;

      const { photoBuffer, mimeType } = getPhoto(body.portrait);
      await uploadPhoto(photoBuffer, itemId, bucketName, mimeType);

      const data = buildDataFromRequestBody(body, s3Uri);
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
        "An error happened processing Driving Licence document request",
      );
      res.render("500.njk");
    }
  };
}

function buildDataFromRequestBody(
  body: DrivingLicenceRequestBody,
  s3Uri: string,
): DrivingLicenceData {
  const birthDay = body["birth-day"];
  const birthMonth = body["birth-month"];
  const birthYear = body["birth-year"];
  const issueDay = body["issue-day"];
  const issueMonth = body["issue-month"];
  const issueYear = body["issue-year"];
  const expiryDay = body["expiry-day"];
  const expiryMonth = body["expiry-month"];
  const expiryYear = body["expiry-year"];

  const fullDrivingPrivileges = getFullDrivingPrivileges(body);
  const provisionalDrivingPrivileges = getProvisionalDrivingPrivileges(body);

  return {
    family_name: body.family_name,
    given_name: body.given_name,
    title: body.title,
    welsh_licence: body.welsh_licence === "true",
    portrait: s3Uri,
    birth_date: formatDate(birthDay, birthMonth, birthYear),
    birth_place: body.birth_place,
    issue_date: formatDate(issueDay, issueMonth, issueYear),
    expiry_date: formatDate(expiryDay, expiryMonth, expiryYear),
    issuing_authority: body.issuing_authority,
    issuing_country: body.issuing_country,
    document_number: body.document_number,
    resident_address: body.resident_address,
    resident_postal_code: body.resident_postal_code,
    resident_city: body.resident_city,
    driving_privileges: fullDrivingPrivileges,
    ...(provisionalDrivingPrivileges.length !== 0 && {
      provisional_driving_privileges: provisionalDrivingPrivileges,
    }),
    un_distinguishing_sign: "UK",
  };
}
