import { expect } from '@jest/globals';
import '../utils/matchers';
import { Context } from 'aws-lambda';
import {
  IssueDocumentSigningCertificateDependencies,
  lambdaHandlerConstructor,
} from '../../issueDocumentSigningCertificate';
import { IssueDocumentSigningCertificateConfig } from '../../issueDocumentSigningCertificateConfig';
import { logger } from '../../logging/logger';
import { headObject, putObject } from '../../adapters/aws/s3Adapter';
import { LogMessage } from '../../logging/LogMessages';
import { createCertificateRequestFromEs256KmsKey } from '../../adapters/peculiar/peculiarAdapter';
import {
  issueMdlDocSigningCertificateUsingSha256WithEcdsa,
  retrieveIssuedCertificate,
} from '../../adapters/aws/acmPcaAdapter';
import { getSsmParameter } from '../../adapters/aws/ssmAdapter';
import { getPublicKey } from '../../adapters/aws/kmsAdapter';

jest.mock('../../adapters/aws/ssmAdapter');
jest.mock('../../adapters/aws/s3Adapter');
jest.mock('../../adapters/aws/kmsAdapter');
jest.mock('../../adapters/aws/acmPcaAdapter');
jest.mock('../../adapters/peculiar/peculiarAdapter');

jest.mock('../../logging/logger', () => {
  return {
    logger: {
      addContext: jest.fn(),
      error: jest.fn(),
      info: jest.fn(),
    },
  };
});

const LAMBDA_CONTEXT = {
  callbackWaitsForEmptyEventLoop: true,
  functionName: 'service',
  functionVersion: '1',
  invokedFunctionArn: 'arn:12345',
  memoryLimitInMB: '1028',
  awsRequestId: '',
  logGroupName: 'logGroup',
  logStreamName: 'logStream',
  getRemainingTimeInMillis: () => {
    return 2000;
  },
  done: function (): void {},
  fail: function (): void {},
  succeed: function (): void {},
};

function getIssueDocumentSigningCertificateConfig(): IssueDocumentSigningCertificateConfig {
  return {
    DOC_SIGNING_KEY_COMMON_NAME: 'commonName',
    DOC_SIGNING_KEY_COUNTRY_NAME: 'UK',
    DOC_SIGNING_KEY_VALIDITY_PERIOD: '100',
    PLATFORM_CA_ARN_PARAMETER: 'arn::ca',
    PLATFORM_CA_ISSUER_ALTERNATIVE_NAME: 'altNameInAsn1',
    DOC_SIGNING_KEY_ID: 'keyId',
    DOC_SIGNING_KEY_BUCKET: 'bucket',
    ROOT_CERTIFICATE: 'root-certificate',
  };
}

const requestEvent = {};
const context: Context = LAMBDA_CONTEXT;

let dependencies: IssueDocumentSigningCertificateDependencies;

describe('issueDocumentSigningCertificate handler', () => {
  beforeEach(() => {
    dependencies = {
      env: getIssueDocumentSigningCertificateConfig(),
    };
    jest
      .mocked(getSsmParameter)
      .mockResolvedValueOnce('CA_ARN')
      .mockResolvedValueOnce('CA_ISSUER_ALTERNATIVE_NAME')
      .mockResolvedValueOnce('ROOT_CERTIFICATE');
    jest.mocked(headObject).mockResolvedValue(false);
    jest.mocked(getPublicKey).mockResolvedValue(Buffer.from('PUBLIC_KEY'));
    jest.mocked(retrieveIssuedCertificate).mockResolvedValue('CERTIFICATE');
    jest.mocked(issueMdlDocSigningCertificateUsingSha256WithEcdsa).mockResolvedValue('CERT_ARN');
    jest.mocked(createCertificateRequestFromEs256KmsKey).mockResolvedValue('CSR');
  });

  describe('When processing starts', () => {
    it('issues a certificate', async () => {
      // ACT
      await lambdaHandlerConstructor(dependencies)(requestEvent, context);

      // ASSERT
      expect(logger.info).toHaveBeenCalledWith(LogMessage.DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ISSUED);
      expect(putObject).toHaveBeenNthCalledWith(1, 'bucket', 'CA_ARN/certificate.pem', 'ROOT_CERTIFICATE');
      expect(putObject).toHaveBeenNthCalledWith(2, 'bucket', 'keyId' + '/certificate.pem', 'CERTIFICATE');
      expect(createCertificateRequestFromEs256KmsKey).toHaveBeenCalledWith('commonName', 'UK', 'keyId');
      expect(issueMdlDocSigningCertificateUsingSha256WithEcdsa).toBeCalledWith(
        'CA_ISSUER_ALTERNATIVE_NAME',
        'CA_ARN',
        Buffer.from('CSR'),
        100,
      );
      expect(retrieveIssuedCertificate).toHaveBeenCalledWith('CERT_ARN', 'CA_ARN');
    });

    it('adds context, service, correlation ID and function version to log attributes', async () => {
      // ACT
      await lambdaHandlerConstructor(dependencies)(requestEvent, context);

      // ASSERT
      expect(logger.addContext).toHaveBeenCalledWith(context);
    });

    it('should emit a DOC_SIGNING_CERT_ISSUER_STARTED message', async () => {
      // ACT
      await lambdaHandlerConstructor(dependencies)(requestEvent, context);

      // ASSERT
      expect(logger.info).toHaveBeenCalledWith(LogMessage.DOC_SIGNING_CERT_ISSUER_STARTED);
    });

    it.each([
      'PLATFORM_CA_ARN_PARAMETER',
      'PLATFORM_CA_ISSUER_ALTERNATIVE_NAME',
      'DOC_SIGNING_KEY_ID',
      'DOC_SIGNING_KEY_BUCKET',
      'DOC_SIGNING_KEY_VALIDITY_PERIOD',
      'DOC_SIGNING_KEY_COMMON_NAME',
      'DOC_SIGNING_KEY_COUNTRY_NAME',
    ])('should emit an error and reject if env var %s is missing', async (envVar) => {
      // ARRANGE
      delete dependencies.env[envVar];

      // ACT
      const promise = lambdaHandlerConstructor(dependencies)(requestEvent, context);

      // ASSERT
      await expect(promise).rejects.toEqual(Error('Invalid configuration'));
      expect(logger.error).toHaveBeenCalledWith(LogMessage.DOC_SIGNING_CERT_ISSUER_CONFIGURATION_FAILED);
    });

    it('should log a message and continue when the root certificate already exists', async () => {
      // ARRANGE: Root certificate exists in bucket
      jest.mocked(headObject).mockResolvedValueOnce(true);

      // ACT & ASSERT
      await expect(lambdaHandlerConstructor(dependencies)(requestEvent, context)).resolves.not.toThrow();

      // ASSERT
      expect(logger.info).toHaveBeenNthCalledWith(3, LogMessage.ROOT_CERTIFICATE_ALREADY_EXISTS);
      expect(putObject).toHaveBeenCalledTimes(1);
    });

    it("should log a message and upload root certificate when it doesn't exist", async () => {
      // ARRANGE: Root certificate does not exist in bucket
      jest.mocked(headObject).mockResolvedValueOnce(false);

      // ACT
      await lambdaHandlerConstructor(dependencies)(requestEvent, context);

      // ASSERT
      expect(logger.info).toHaveBeenNthCalledWith(3, LogMessage.ROOT_CERTIFICATE_UPLOADED);
      expect(putObject).toHaveBeenCalledTimes(2);
    });

    it('should emit an error and reject if the certificate has already been issued for this key', async () => {
      // ARRANGE
      jest.mocked(headObject).mockResolvedValueOnce(true); // root certificate
      jest.mocked(headObject).mockResolvedValueOnce(true); // document signing certificate

      // ACT
      const promise = lambdaHandlerConstructor(dependencies)(requestEvent, context);

      // ASSERT
      await expect(promise).rejects.toEqual(Error('Certificate already exists for this KMS Key'));
      expect(logger.error).toHaveBeenCalledWith(LogMessage.DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ALREADY_EXISTS);
    });

    it('should emit an error and reject if unable to create a CSR', async () => {
      // ARRANGE
      jest.mocked(createCertificateRequestFromEs256KmsKey).mockRejectedValueOnce(false);

      // ACT
      const promise = lambdaHandlerConstructor(dependencies)(requestEvent, context);

      // ASSERT
      await expect(promise).rejects.toEqual(false);
      expect(logger.error).toHaveBeenCalledWith(LogMessage.DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ISSUE_FAILED, {
        data: false,
      });
    });

    it('should emit an error and reject if unable to issue a certificate', async () => {
      // ARRANGE
      jest.mocked(issueMdlDocSigningCertificateUsingSha256WithEcdsa).mockRejectedValueOnce(false);

      // ACT
      const promise = lambdaHandlerConstructor(dependencies)(requestEvent, context);

      // ASSERT
      await expect(promise).rejects.toEqual(false);
      expect(logger.error).toHaveBeenCalledWith(LogMessage.DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ISSUE_FAILED, {
        data: false,
      });
    });

    it('should emit an error and reject if unable to retrieve the certificate', async () => {
      // ARRANGE
      jest.mocked(retrieveIssuedCertificate).mockRejectedValueOnce(false);

      // ACT
      const promise = lambdaHandlerConstructor(dependencies)(requestEvent, context);

      // ASSERT
      await expect(promise).rejects.toEqual(false);
      expect(logger.error).toHaveBeenCalledWith(LogMessage.DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ISSUE_FAILED, {
        data: false,
      });
    });
  });
});
