import { Buffer } from 'buffer';
import * as x509 from '@peculiar/x509';
import {
  ACMPCAClient,
  GetCertificateCommand,
  GetCertificateCommandOutput,
  IssueCertificateCommand,
  IssueCertificateCommandOutput,
  RequestInProgressException,
} from '@aws-sdk/client-acm-pca';
import { KMSClient } from '@aws-sdk/client-kms';
import { Pkcs10CertificateRequestGeneratorUsingKmsKey } from './services/Pkcs10CertificateRequestGeneratorUsingKmsKey';
import { logger } from './logging/logger';
import { Context } from 'aws-lambda';
import { getConfigFromEnvironment } from './issueDocumentSigningCertificateConfig';
import { LogMessage } from './logging/LogMessages';
import { headObject, putObject } from './adapters/aws/s3Adapter';
import { getSsmParameter } from './adapters/aws/ssmAdapter';

const pcaClient = new ACMPCAClient();
const kmsClient = new KMSClient();

export type IssueDocumentSigningCertificateDependencies = {
  env: NodeJS.ProcessEnv;
};

const dependencies: IssueDocumentSigningCertificateDependencies = {
  env: process.env,
};

export const handler = lambdaHandlerConstructor(dependencies);

export function lambdaHandlerConstructor(dependencies: IssueDocumentSigningCertificateDependencies) {
  return async (_event: any, context: Context) => {
    logger.addContext(context);
    logger.info('STARTING');

    const configResult = getConfigFromEnvironment(dependencies.env);
    if (configResult.isError) {
      logger.error(LogMessage.APP_CHECK_INCIDENT_CHECKER_FAILED_TO_DETERMINE_APP_CHECK_STATUS, {
        data: { reason: 'Invalid Config' },
      });
      return;
    }
    const config = configResult.value;
    logger.info('CONFIG', { config });

    const certificateAuthorityArn = await getSsmParameter(config.PLATFORM_CA_ARN_PARAMETER);
    const issuerAlternativeName = await getSsmParameter(config.PLATFORM_CA_ISSUER_ALTERNATIVE_NAME);

    // Abort if certificate already exists in the bucket
    if (await headObject(config.DOC_SIGNING_KEY_BUCKET, config.DOC_SIGNING_KEY_ID + '/certificate.pem')) {
      logger.error('ABORTED - CERTIFICATE ALREADY EXISTS FOR THIS KMS KEY');
      return;
    }

    // Generate CSR
    const alg = {
      name: 'ECDSA',
      namedCurve: 'P-256',
      hash: 'SHA-256',
    };
    const csr = await Pkcs10CertificateRequestGeneratorUsingKmsKey.create(
      {
        name: [{ CN: ['Test Certificate'] }, { C: ['UK'] }],
        signingAlgorithm: alg,
        extensions: [
          new x509.KeyUsagesExtension(x509.KeyUsageFlags.digitalSignature | x509.KeyUsageFlags.keyEncipherment),
        ],
      },
      config.DOC_SIGNING_KEY_ID,
      kmsClient,
    );

    logger.info('CSR', { string: csr.toString() });

    // Send Request to AWS Private CA to issue Certificate
    const issueCertificateCommand = new IssueCertificateCommand({
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
              Value: issuerAlternativeName,
            },
          ],
        },
      },
      CertificateAuthorityArn: certificateAuthorityArn,
      Csr: Buffer.from(csr.toString()),
      SigningAlgorithm: 'SHA256WITHECDSA',
      TemplateArn: 'arn:aws:acm-pca:::template/BlankEndEntityCertificate_APIPassthrough/V1',
      Validity: {
        Value: 1825,
        Type: 'DAYS',
      },
    });

    logger.info('ISSUING CERTIFICATE');
    let issueCertificateCommandOutput: IssueCertificateCommandOutput;
    try {
      issueCertificateCommandOutput = await pcaClient.send(issueCertificateCommand);
    } catch (e) {
      logger.info('Exception in Issuing Certificate', { e });
      throw e;
    }
    logger.info('ISSUE CERTIFICATE COMMAND ACCEPTED');

    let getCertificateCommandOutput: GetCertificateCommandOutput;
    while (true) {
      const getCertificateCommand = new GetCertificateCommand({
        CertificateArn: issueCertificateCommandOutput.CertificateArn,
        CertificateAuthorityArn: certificateAuthorityArn,
      });
      try {
        getCertificateCommandOutput = await pcaClient.send(getCertificateCommand);
        break;
      } catch (e) {
        if (RequestInProgressException.isInstance(e)) {
          continue;
        }
        throw e;
      }
    }

    logger.info('CERTIFICATE RETRIEVED', { getCertificateCommandOutput });
    await putObject(
      config.DOC_SIGNING_KEY_BUCKET,
      config.DOC_SIGNING_KEY_ID + '/certificate.pem',
      getCertificateCommandOutput.Certificate,
    );
    logger.info('CERTIFICATE WRITTEN TO S3');
  };
}
