import { Request, Response } from "express";
import { randomUUID } from "node:crypto";
import {
  getDocumentsTableName,
  getPhotosBucketName,
  getTableItemTtl,
} from "../config/appConfig";
import { getPhoto } from "../utils/photoUtils";
import { uploadPhoto } from "../services/s3Service";
import { buildDefaultDrivingLicenceData } from "./helpers/buildDefaultDrivingLicenceData";
import { saveDocument } from "../services/databaseService";
import { getTimeToLiveEpoch } from "../utils/getTimeToLiveEpoch";
import { getViewCredentialOfferRedirectUrl } from "../utils/getViewCredentialOfferRedirectUrl";
import { CredentialType } from "../types/CredentialType";
import { logger } from "../middleware/logger";

const DOCUMENT_PHOTO_FILENAME = "dvs.jpeg";
const CREDENTIAL_TYPE = CredentialType.MobileDrivingLicence;
const CREDENTIAL_TTL_SECONDS = 172800; // 48 hours

export async function dvsDrivingLicenceBuilderGetController(
  req: Request,
  res: Response,
): Promise<void> {
  try {
    const itemId = randomUUID();
    const bucketName = getPhotosBucketName();
    const s3Uri = `s3://${bucketName}/${itemId}`;

    const { photoBuffer, mimeType } = getPhoto(DOCUMENT_PHOTO_FILENAME);
    await uploadPhoto(photoBuffer, itemId, bucketName, mimeType);

    const drivingLicenceData = buildDefaultDrivingLicenceData(s3Uri);

    await saveDocument(getDocumentsTableName(), {
      itemId,
      documentId: drivingLicenceData.document_number,
      data: drivingLicenceData,
      vcType: CREDENTIAL_TYPE,
      credentialTtlSeconds: CREDENTIAL_TTL_SECONDS,
      timeToLive: getTimeToLiveEpoch(getTableItemTtl()),
    });

    const redirectUrl = getViewCredentialOfferRedirectUrl({
      itemId,
      credentialType: CREDENTIAL_TYPE,
      isDvsRoute: true,
    });
    res.redirect(redirectUrl);
  } catch (error) {
    logger.error(
      error,
      "An error happened processing the DVS Driving Licence document request",
    );
    res.render("500.njk");
  }
}
