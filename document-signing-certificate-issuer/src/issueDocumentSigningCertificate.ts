import { createCertificateRequestFromEs256KmsKey } from './adapters/peculiar/peculiarAdapter';
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
  return async (_event: unknown, context: Context) => {
    logger.addContext(context);
    logger.info(LogMessage.DOC_SIGNING_CERT_ISSUER_STARTED);

    console.log("test");

    const configResult = getConfigFromEnvironment(dependencies.env);
    if (configResult.isError) {
      logger.error(LogMessage.DOC_SIGNING_CERT_ISSUER_CONFIGURATION_FAILED);
      throw new Error('Invalid configuration');
    }
    const config = configResult.value;
    logger.info(LogMessage.DOC_SIGNING_CERT_ISSUER_CONFIGURATION_SUCCESS, { config });

    const certificateAuthorityArn = await getSsmParameter(config.PLATFORM_CA_ARN_PARAMETER);
    const issuerAlternativeName = await getSsmParameter(config.PLATFORM_CA_ISSUER_ALTERNATIVE_NAME);

    if (await headObject(config.DOC_SIGNING_KEY_BUCKET, config.DOC_SIGNING_KEY_ID + '/certificate.pem')) {
      logger.error(LogMessage.DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ALREADY_EXISTS);
      throw new Error('Certificate already exists for this KMS Key');
    }

    try {
      const csr = await createCertificateRequestFromEs256KmsKey(
        config.DOC_SIGNING_KEY_COMMON_NAME,
        config.DOC_SIGNING_KEY_COUNTRY_NAME,
        config.DOC_SIGNING_KEY_ID,
      );

      const issuedCertificateArn = await issueMdlDocSigningCertificateUsingSha256WithEcdsa(
        issuerAlternativeName,
        certificateAuthorityArn,
        Buffer.from(csr),
        Number(config.DOC_SIGNING_KEY_VALIDITY_PERIOD),
      );

      const issuedCertificate = await retrieveIssuedCertificate(issuedCertificateArn, certificateAuthorityArn);
      await putObject(config.DOC_SIGNING_KEY_BUCKET, config.DOC_SIGNING_KEY_ID + '/certificate.pem', issuedCertificate);

      logger.info(LogMessage.DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ISSUED);
    } catch (error) {
      logger.error(LogMessage.DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ISSUE_FAILED, { data: error });
      throw error;
    }
  };
}
