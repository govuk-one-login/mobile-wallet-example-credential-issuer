import { LogAttributes } from '@aws-lambda-powertools/logger/types';

export class LogMessage implements LogAttributes {
  static readonly DOC_SIGNING_CERT_ISSUER_STARTED = new LogMessage(
    'DOC_SIGNING_CERT_ISSUER_STARTED',
    'Document Signing Certificate Issuer Lambda has started.',
    'N/A',
  );
  static readonly DOC_SIGNING_CERT_ISSUER_CONFIGURATION_FAILED = new LogMessage(
    'DOC_SIGNING_CERT_ISSUER_CONFIGURATION_FAILED',
    'Document Signing Certificate Issuer Lambda environment variable configuration is incorrect',
    'Unable is issue Document Signing Certificate',
  );
  static readonly DOC_SIGNING_CERT_ISSUER_CONFIGURATION_SUCCESS = new LogMessage(
    'DOC_SIGNING_CERT_ISSUER_CONFIGURATION_SUCCESS',
    'Document Signing Certificate Issuer Lambda successfully configured',
    'N/A',
  );
  static readonly DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ALREADY_EXISTS = new LogMessage(
    'DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ALREADY_EXISTS',
    'Document Signing Certificate Issuer aborted since a certificate already exists for this KMS key',
    'Unable to issue certificate - either use the existing certificate or create a new KMS key',
  );
  static readonly DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ISSUE_FAILED = new LogMessage(
    'DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ISSUE_FAILED',
    'Document Signing Certificate Issuer was unable to issue a certificate',
    'Investigate log messages for further details',
  );
  static readonly DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ISSUED = new LogMessage(
    'DOC_SIGNING_CERT_ISSUER_CERTIFICATE_ISSUED',
    'Document Signing Certificate Issuer successfully issued a new certificate',
    'N/A',
  );
  static readonly ROOT_CERTIFICATE_ALREADY_EXISTS = new LogMessage(
    'ROOT_CERTIFICATE_ALREADY_EXISTS',
    'Root certificate already in S3 bucket',
    'N/A',
  );
  private constructor(
    public readonly messageCode: string,
    public readonly message: string,
    public readonly userImpact: string,
  ) {}

  [key: string]: string; // Index signature needed to implement LogAttributesWithMessage
}
