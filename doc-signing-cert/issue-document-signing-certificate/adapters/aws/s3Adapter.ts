import { HeadObjectCommand, NotFound, PutObjectCommand, S3Client } from '@aws-sdk/client-s3';

const s3Client = new S3Client();

export async function putObject(bucket: string, key: string, body: string | undefined) {
  const putObjectCommand = new PutObjectCommand({
    Bucket: bucket,
    Key: key,
    Body: body,
  });
  await s3Client.send(putObjectCommand);
}

export async function headObject(bucket: string, key: string) {
  try {
    const headObjectCommand = new HeadObjectCommand({
      Bucket: bucket,
      Key: key,
    });
    await s3Client.send(headObjectCommand);
  } catch (e) {
    if (NotFound.isInstance(e)) {
      return false;
    }
    throw e;
  }
  return true;
}
