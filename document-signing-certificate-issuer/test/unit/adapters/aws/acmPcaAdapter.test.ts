import {
  ACMPCAClient,
  GetCertificateCommand,
  IssueCertificateCommand,
  RequestInProgressException,
} from '@aws-sdk/client-acm-pca';
import { mockClient } from 'aws-sdk-client-mock';
import 'aws-sdk-client-mock-jest';
import {
  issueMdlDocSigningCertificateUsingSha256WithEcdsa,
  retrieveIssuedCertificate,
} from '../../../../src/adapters/aws/acmPcaAdapter';

const mockAcmPcaClient = mockClient(ACMPCAClient);

describe('acmPcaAdapter', () => {
  beforeEach(() => {
    mockAcmPcaClient.reset();
  });

  describe('issueMdlDocSigningCertificateUsingSha256WithEcdsa', () => {
    it('should pass the call on to the AWS API and return the CertificateArn', async () => {
      // ARRANGE
      mockAcmPcaClient.on(IssueCertificateCommand).resolves({
        CertificateArn: 'CERTIFICATE_ARN',
      });

      // ACT
      const response = await issueMdlDocSigningCertificateUsingSha256WithEcdsa(
        'ISSUER_ALT_NAME',
        'CA_ARN',
        Buffer.from('CSR'),
        500,
      );

      // ASSERT
      expect(response).toEqual('CERTIFICATE_ARN');
      expect(mockAcmPcaClient).toHaveReceivedCommandWith(IssueCertificateCommand, {
        ApiPassthrough: {
          Extensions: {
            KeyUsage: {
              DigitalSignature: true,
            },
            ExtendedKeyUsage: [
              {
                ExtendedKeyUsageObjectIdentifier: '1.0.18013.5.1.2', // identifier for ISO mDL
              },
            ],
            CustomExtensions: [
              {
                ObjectIdentifier: '2.5.29.18',
                Value: 'ISSUER_ALT_NAME',
              },
            ],
          },
        },
        CertificateAuthorityArn: 'CA_ARN',
        Csr: Buffer.from('CSR'),
        SigningAlgorithm: 'SHA256WITHECDSA',
        TemplateArn: 'arn:aws:acm-pca:::template/BlankEndEntityCertificate_APIPassthrough/V1',
        Validity: {
          Value: 500,
          Type: 'DAYS',
        },
      });
    });

    it('should reject if the AWS API rejects', async () => {
      // ARRANGE
      mockAcmPcaClient.on(IssueCertificateCommand).rejects('REJECTED');

      // ACT
      const promise = issueMdlDocSigningCertificateUsingSha256WithEcdsa(
        'ISSUER_ALT_NAME',
        'CA_ARN',
        Buffer.from('CSR'),
        500,
      );

      // ASSERT
      return expect(promise).rejects.toEqual(Error('REJECTED'));
    });

    it('should reject if the CertificateArn is not returned', async () => {
      // ARRANGE
      mockAcmPcaClient.on(IssueCertificateCommand).resolves({
        CertificateArn: undefined,
      });

      // ACT
      const promise = issueMdlDocSigningCertificateUsingSha256WithEcdsa(
        'ISSUER_ALT_NAME',
        'CA_ARN',
        Buffer.from('CSR'),
        500,
      );

      // ASSERT
      return expect(promise).rejects.toEqual(Error('Failed to issue certificate'));
    });
  });

  describe('retrieveIssuedCertificate', () => {
    beforeEach(() => {
      jest.useFakeTimers();
    });

    afterEach(() => {
      jest.useRealTimers();
    });

    it('should pass the call on to the AWS API and return the certificate', async () => {
      // ARRANGE
      mockAcmPcaClient.on(GetCertificateCommand).resolves({
        Certificate: 'BEGIN_CERTIFICATE',
      });

      // ACT
      const response = await retrieveIssuedCertificate('ISSUED_CERT_ARN', 'CA_ARN');

      // ASSERT
      expect(response).toEqual('BEGIN_CERTIFICATE');
      expect(mockAcmPcaClient).toHaveReceivedCommandWith(GetCertificateCommand, {
        CertificateArn: 'ISSUED_CERT_ARN',
        CertificateAuthorityArn: 'CA_ARN',
      });
    });

    it('should reject if the AWS API call rejects', async () => {
      // ARRANGE
      mockAcmPcaClient.on(GetCertificateCommand).rejects('REJECTED');

      // ACT
      const promise = retrieveIssuedCertificate('ISSUED_CERT_ARN', 'CA_ARN');

      // ASSERT
      return expect(promise).rejects.toEqual(Error('REJECTED'));
    });

    it('should reject if the Certificate is not returned', async () => {
      // ARRANGE
      mockAcmPcaClient.on(GetCertificateCommand).resolves({
        Certificate: undefined,
      });

      // ACT
      const promise = retrieveIssuedCertificate('ISSUED_CERT_ARN', 'CA_ARN');

      // ASSERT
      return expect(promise).rejects.toEqual(Error('Failed to retrieve certificate'));
    });

    it('should retry if a RequestInProgressException is thrown', async () => {
      // ARRANGE
      mockAcmPcaClient
        .on(GetCertificateCommand)
        .rejectsOnce(
          new RequestInProgressException({
            $metadata: {},
            message: '',
          }),
        )
        .resolves({
          Certificate: 'BEGIN_CERTIFICATE',
        });

      // ACT
      await retrieveIssuedCertificate('ISSUED_CERT_ARN', 'CA_ARN');

      // ASSERT
      expect(mockAcmPcaClient).toHaveReceivedCommandTimes(GetCertificateCommand, 2);
    });

    it('should timeout if the retrying exceeds the timeout limit', async () => {
      // ARRANGE
      mockAcmPcaClient.on(GetCertificateCommand).rejects(
        new RequestInProgressException({
          $metadata: {},
          message: '',
        }),
      );

      // ACT
      const promise = retrieveIssuedCertificate('ISSUED_CERT_ARN', 'CA_ARN', 1000);
      jest.runAllTimers();

      // ASSERT
      return expect(promise).rejects.toEqual(Error('Request timed out'));
    }, 1500);
  });
});
