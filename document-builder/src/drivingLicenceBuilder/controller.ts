import { NextFunction, Request, Response } from "express";
import {
  getDocumentsTableName,
  getEnvironment,
  getPhotosBucketName,
  getTableItemTtl,
} from "../config/appConfig";
import { CredentialType } from "../types/CredentialType";
import { isAuthenticated } from "../utils/isAuthenticated";
import { randomUUID } from "node:crypto";
import { saveDocument } from "../services/databaseService";
import { getDefaultDates, formatDate } from "../utils/date";
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
import { calculateCredentialTtlSeconds } from "../utils/calculateCredentialTtlSeconds";
import { validateDrivingLicenceForm } from "./helpers/DrivingLicenceFormValidator";
import {
  CUSTOM_CREDENTIAL_TTL,
  SECONDS_IN_A_DAY,
} from "../config/credentialTtl";
import { ENVIRONMENTS } from "../config/environments";

const CREDENTIAL_TYPE = CredentialType.MobileDrivingLicence;

export interface DrivingLicenceBuilderControllerConfig {
  environment?: string;
}

export function drivingLicenceBuilderGetController({
  environment = getEnvironment(),
}: DrivingLicenceBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> {
    try {
      const { defaultIssueDate, defaultExpiryDate } = getDefaultDates();
      res.render("driving-licence-form.njk", {
        defaultIssueDate,
        defaultExpiryDate,
        drivingLicenceNumber: "EDWAR" + getRandomIntInclusive() + "SE5RO",
        authenticated: isAuthenticated(req),
        errorChoices: ERROR_CHOICES,
        showThrowError: environment !== ENVIRONMENTS.STAGE,
      });
    } catch (error) {
      next(
        new Error("An error happened rendering Driving Licence document page", {
          cause: error,
        }),
      );
    }
  };
}

export function drivingLicenceBuilderPostController({
  environment = getEnvironment(),
}: DrivingLicenceBuilderControllerConfig = {}): ExpressRouteFunction {
  return async function (
    req: Request,
    res: Response,
    next: NextFunction,
  ): Promise<void> {
    try {
      const body: DrivingLicenceRequestBody = req.body;

      const result = validateDrivingLicenceForm(body);
      if (!result.isValid) {
        const { defaultIssueDate, defaultExpiryDate } = getDefaultDates();
        return res.render("driving-licence-form.njk", {
          defaultIssueDate,
          defaultExpiryDate,
          drivingLicenceNumber: body.document_number,
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

      const data = buildDataFromRequestBody(body, s3Uri);
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
        documentId: data.document_number,
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
          "An error happened processing Driving Licence document request",
          { cause: error },
        ),
      );
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
