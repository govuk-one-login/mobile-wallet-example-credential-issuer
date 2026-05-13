import {
  S3Client,
  PutObjectCommand,
  GetObjectCommand,
} from "@aws-sdk/client-s3";
import { getS3Config } from "../config/aws";
import { logger } from "../middleware/logger";

const s3Client = new S3Client(getS3Config());

export async function uploadPhoto(
  imageBuffer: Buffer,
  imageName: string,
  bucketName: string,
  contentType: string,
): Promise<void> {
  const putObjectCommandInput = {
    Bucket: bucketName,
    Key: imageName,
    Body: imageBuffer,
    ContentType: contentType,
  };

  await s3Client.send(new PutObjectCommand(putObjectCommandInput));
}

export async function getPhoto(
  imageName: string,
  bucketName: string,
): Promise<string | undefined> {
  const getObjectCommandInput = {
    Bucket: bucketName,
    Key: imageName,
  };

  const response = await s3Client.send(
    new GetObjectCommand(getObjectCommandInput),
  );
  if (response.Body === undefined) {
    logger.info(`Object with key ${imageName} not found`);
    return undefined;
  }
  return await response.Body.transformToString("base64");
}
