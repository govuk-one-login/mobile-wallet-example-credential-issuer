import { mockClient } from 'aws-sdk-client-mock';
import 'aws-sdk-client-mock-jest';
import { GetPublicKeyCommand, KMSClient, SignCommand } from '@aws-sdk/client-kms';
import { getPublicKey, signWithEcdsaSha256 } from '../../../../src/adapters/aws/kmsAdapter';

const mockKmsClient = mockClient(KMSClient);

describe('kmsAdapter', () => {
  beforeEach(() => {
    mockKmsClient.reset();
  });

  describe('sign', () => {
    it('should pass the call on to the KMS API', async () => {
      // ARRANGE
      mockKmsClient.on(SignCommand).resolves({
        Signature: Buffer.from('SIGNED_BUFFER'),
      });

      // ACT
      const response = await signWithEcdsaSha256('KEYID', Buffer.from('BUFFER_TO_SIGN'));

      // ASSERT
      expect(response).toEqual(Buffer.from('SIGNED_BUFFER'));
      expect(mockKmsClient).toHaveReceivedCommandWith(SignCommand, {
        KeyId: 'KEYID',
        Message: Buffer.from('BUFFER_TO_SIGN'),
        SigningAlgorithm: 'ECDSA_SHA_256',
      });
    });

    it('should reject if AWS rejects', async () => {
      // ARRANGE
      mockKmsClient.on(SignCommand).rejects('REJECTED');

      // ACT
      const promise = signWithEcdsaSha256('KEYID', Buffer.from('BUFFER_TO_SIGN'));

      // ASSERT
      expect(mockKmsClient).toHaveReceivedCommandTimes(SignCommand, 1);
      return expect(promise).rejects.toEqual(Error('REJECTED'));
    });

    it('should reject if KMS does not return a Signature', async () => {
      // ARRANGE
      mockKmsClient.on(SignCommand).resolves({
        Signature: undefined,
      });

      // ACT
      const promise = signWithEcdsaSha256('KEYID', Buffer.from('BUFFER_TO_SIGN'));

      // ASSERT
      expect(mockKmsClient).toHaveReceivedCommandTimes(SignCommand, 1);
      return expect(promise).rejects.toEqual(Error('An error occurred when signing the request with KMS'));
    });
  });

  describe('getPublicKey', () => {
    it('should pass the call on to the KMS API', async () => {
      // ARRANGE
      mockKmsClient.on(GetPublicKeyCommand).resolves({
        PublicKey: Buffer.from('PUBLIC_KEY'),
      });

      // ACT
      const response = await getPublicKey('KEYID');

      // ASSERT
      expect(response).toEqual(Buffer.from('PUBLIC_KEY'));
      expect(mockKmsClient).toHaveReceivedCommandWith(GetPublicKeyCommand, {
        KeyId: 'KEYID',
      });
    });

    it('should reject if AWS rejects', async () => {
      // ARRANGE
      mockKmsClient.on(GetPublicKeyCommand).rejects('REJECTED');

      // ACT
      const promise = getPublicKey('KEYID');

      // ASSERT
      expect(mockKmsClient).toHaveReceivedCommandTimes(GetPublicKeyCommand, 1);
      return expect(promise).rejects.toEqual(Error('REJECTED'));
    });

    it('should reject if the public key is not returned', async () => {
      // ARRANGE
      mockKmsClient.on(GetPublicKeyCommand).resolves({
        PublicKey: undefined,
      });

      // ACT
      const promise = getPublicKey('KEYID');

      // ASSERT
      expect(mockKmsClient).toHaveReceivedCommandTimes(GetPublicKeyCommand, 1);
      return expect(promise).rejects.toEqual(Error('Error retrieving public key from KMS'));
    });
  });
});
