import { Request, Response } from "express";
import { getDocument } from "../services/databaseService";
import { logger } from "../middleware/logger";
import { CredentialType } from "../types/CredentialType";
import { getDocumentsTableName } from "../config/appConfig";
import { getPhotoFromS3 } from "../services/photoService";

export async function documentController(
  req: Request,
  res: Response,
): Promise<void> {
  try {
    const itemId = req.params.itemId as string;
    const tableName = getDocumentsTableName();
    const tableItem = await getDocument(tableName, itemId);

    if (!tableItem) {
      logger.error(`Document with ID ${itemId} not found`);
      res.status(404).send();
      return;
    }

    const { data } = tableItem;

    if (
      tableItem.vcType === CredentialType.DigitalVeteranCard ||
      tableItem.vcType === CredentialType.MobileDrivingLicence ||
      tableItem.vcType === CredentialType.SimpleDocument
    ) {
      const typedData = data as { portrait: string };
      const photoBase64 = await getPhotoFromS3(typedData.portrait, itemId);

      if (!photoBase64) {
        res.status(404).send();
        return;
      }

      typedData.portrait = photoBase64;
    }

    res.status(200).json(tableItem);

    return;
  } catch (error) {
    logger.error(error, "An error happened processing request to get document");
    res.status(500).send();
    return;
  }
}
