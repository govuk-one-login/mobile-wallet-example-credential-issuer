import { getPhoto } from "../services/s3Service";
import { logger } from "../middleware/logger";

export async function getPhotoFromS3(
  photoUri: string,
  itemId: string,
): Promise<string | null> {
  const { bucketName, fileName } = getBucketAndFileName(photoUri);

  const photo = await getPhoto(fileName, bucketName);

  if (!photo) {
    logger.error(`Photo for document with ID ${itemId} not found`);
    return null;
  }

  return photo;
}

function getBucketAndFileName(s3Uri: string): {
  bucketName: string;
  fileName: string;
} {
  const s3UriParts = s3Uri.split("/");
  const bucketName = s3UriParts[2];
  const fileName = s3UriParts[3];
  return { bucketName, fileName };
}
