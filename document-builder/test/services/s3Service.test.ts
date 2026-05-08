process.env.ENVIRONMENT = "local";
import { mockClient } from "aws-sdk-client-mock";
import {
  S3Client,
  PutObjectCommand,
  GetObjectCommand,
  GetObjectCommandOutput,
} from "@aws-sdk/client-s3";
import { uploadPhoto, getPhoto } from "../../src/services/s3Service";
import "aws-sdk-client-mock-jest";

const bucketName = "bucketName";
const imageName = "fileName";
const imageBuffer = Buffer.alloc(10);
const mimeType = "image/jpeg";

describe("s3Service.ts", () => {
  describe("Upload Photo", () => {
    it("should save the photo to the S3 bucket", async () => {
      const s3Mock = mockClient(S3Client);
      s3Mock.on(PutObjectCommand).resolvesOnce({
        $metadata: {
          httpStatusCode: 200,
        },
      });

      await expect(
        uploadPhoto(imageBuffer, imageName, bucketName, mimeType),
      ).resolves.not.toThrow();
      expect(s3Mock).toHaveReceivedCommandWith(PutObjectCommand, {
        Bucket: bucketName,
        Key: imageName,
        Body: imageBuffer,
      });
    });

    it("should throw the error throw by the S3 client", async () => {
      const s3Mock = mockClient(S3Client);
      s3Mock.on(PutObjectCommand).rejectsOnce("MOCK_S3_PUT_OBJECT_ERROR");

      await expect(
        uploadPhoto(imageBuffer, imageName, bucketName, mimeType),
      ).rejects.toThrow("MOCK_S3_PUT_OBJECT_ERROR");
      expect(s3Mock).toHaveReceivedCommandWith(PutObjectCommand, {
        Bucket: bucketName,
        Key: imageName,
        Body: imageBuffer,
      });
    });
  });

  describe("Get Photo", () => {
    const mockS3Response = (content: string) => {
      return {
        transformToString: async () => content,
      };
    };

    it("should get the photo from the S3 bucket", async () => {
      const s3Mock = mockClient(S3Client);
      s3Mock.on(GetObjectCommand).resolvesOnce({
        Body: mockS3Response("base64image"),
      } as GetObjectCommandOutput);

      const response = await getPhoto(imageName, bucketName);

      expect(response).toEqual("base64image");
    });

    it("it should return undefined if the S3 response Body is undefined", async () => {
      const s3Mock = mockClient(S3Client);
      s3Mock
        .on(GetObjectCommand)
        .resolvesOnce({ Body: undefined } as GetObjectCommandOutput);

      const response = await getPhoto(imageName, bucketName);

      expect(response).toEqual(undefined);
    });

    it("should throw the error throw by the S3 client", async () => {
      const s3Mock = mockClient(S3Client);
      s3Mock.on(GetObjectCommand).rejectsOnce("MOCK_S3_GET_OBJECT_ERROR");

      await expect(getPhoto(imageName, bucketName)).rejects.toThrow(
        "MOCK_S3_GET_OBJECT_ERROR",
      );
      expect(s3Mock).toHaveReceivedCommandWith(GetObjectCommand, {
        Bucket: bucketName,
        Key: imageName,
      });
    });
  });
});
