import { mockClient } from 'aws-sdk-client-mock';
import { HeadObjectCommand, NotFound, PutObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { headObject, putObject } from '../../../../src/adapters/aws/s3Adapter';
import 'aws-sdk-client-mock-jest';

const mockS3Client = mockClient(S3Client);

describe('s3Adapter', () => {
  beforeEach(() => {
    mockS3Client.reset();
  });

  describe('putObject', () => {
    it('should pass the call on to the S3 API', async () => {
      // ARRANGE
      mockS3Client.on(PutObjectCommand).resolves({});

      // ACT
      await putObject('Bucket', 'Key', 'Body');

      // ASSERT
      expect(mockS3Client).toHaveReceivedCommandWith(PutObjectCommand, {
        Bucket: 'Bucket',
        Key: 'Key',
        Body: 'Body',
      });
    });

    it('should reject if the S3 API rejects', async () => {
      // ARRANGE
      mockS3Client.on(PutObjectCommand).rejects('REJECTED');

      // ACT
      const promise = putObject('Bucket', 'Key', 'Body');

      // ASSERT
      return expect(promise).rejects.toEqual(Error('REJECTED'));
    });
  });

  describe('headObject', () => {
    it('should pass the call on to the S3 API and return true if it resolves', async () => {
      // ARRANGE
      mockS3Client.on(HeadObjectCommand).resolves({});

      // ACT
      const response = await headObject('Bucket', 'Key');

      // ASSERT
      expect(mockS3Client).toHaveReceivedCommandWith(HeadObjectCommand, {
        Bucket: 'Bucket',
        Key: 'Key',
      });
      expect(response).toEqual(true);
    });

    it('should return false if the S3 object does not exist', async () => {
      // ARRANGE
      mockS3Client.on(HeadObjectCommand).rejects(new NotFound({ $metadata: {}, message: 'NOT FOUND' }));

      // ACT
      const response = await headObject('Bucket', 'Key');

      // ASSERT
      expect(mockS3Client).toHaveReceivedCommandWith(HeadObjectCommand, {
        Bucket: 'Bucket',
        Key: 'Key',
      });
      expect(response).toEqual(false);
    });

    it('should reject if the S3 API rejects', async () => {
      // ARRANGE
      mockS3Client.on(HeadObjectCommand).rejects('REJECTED');

      // ACT
      const promise = headObject('Bucket', 'Key');

      // ASSERT
      return expect(promise).rejects.toEqual(Error('REJECTED'));
    });
  });
});
