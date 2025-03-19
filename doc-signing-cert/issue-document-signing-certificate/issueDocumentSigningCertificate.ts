import { X509Certificate } from '@peculiar/x509';
import { Pkcs10CertificateRequestGeneratorUsingKmsKey } from './services/Pkcs10CertificateRequestGeneratorUsingKmsKey';
import { logger } from './logging/logger';
import { Context } from 'aws-lambda';
import { getConfigFromEnvironment } from './issueDocumentSigningCertificateConfig';
import { LogMessage } from './logging/LogMessages';
import { headObject, putObject } from './adapters/aws/s3Adapter';
import { getSsmParameter } from './adapters/aws/ssmAdapter';
import {
  issueMdlDocSigningCertificateUsingSha256WithEcdsa,
  retrieveIssuedCertificate,
} from './adapters/aws/acmPcaAdapter';

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
      },
      config.DOC_SIGNING_KEY_ID,
    );

    logger.info('CSR', { string: csr.toString() });

    const issuedCertificateArn = await issueMdlDocSigningCertificateUsingSha256WithEcdsa(
      issuerAlternativeName,
      certificateAuthorityArn,
      Buffer.from(csr.toString()),
      Number(config.DOC_SIGNING_KEY_VALIDITY_PERIOD),
    );

    const issuedCertificate = await retrieveIssuedCertificate(issuedCertificateArn, certificateAuthorityArn);

    await putObject(config.DOC_SIGNING_KEY_BUCKET, config.DOC_SIGNING_KEY_ID + '/certificate.pem', issuedCertificate);

    const decodedCertificate = new X509Certificate(issuedCertificate);
    await putObject(
      config.DOC_SIGNING_KEY_BUCKET,
      config.DOC_SIGNING_KEY_ID + '/certificate-metadata.json',
      JSON.stringify(decodedCertificate),
    );

    logger.info('CERTIFICATE ISSUED AND WRITTEN TO BUCKET');
  };
}
