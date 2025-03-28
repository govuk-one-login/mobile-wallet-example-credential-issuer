import {
  ACMPCAClient,
  GetCertificateCommand,
  GetCertificateCommandOutput,
  IssueCertificateCommand,
  RequestInProgressException,
} from '@aws-sdk/client-acm-pca';

const pcaClient = new ACMPCAClient();

export async function issueMdlDocSigningCertificateUsingSha256WithEcdsa(
  issuerAlternativeName: string,
  certificateAuthorityArn: string,
  csr: Uint8Array<ArrayBufferLike>,
  validityPeriod: number,
) {
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
    Csr: csr,
    SigningAlgorithm: 'SHA256WITHECDSA',
    TemplateArn: 'arn:aws:acm-pca:::template/BlankEndEntityCertificate_APIPassthrough/V1',
    Validity: {
      Value: validityPeriod,
      Type: 'DAYS',
    },
  });

  const issueCertificateCommandOutput = await pcaClient.send(issueCertificateCommand);
  if (issueCertificateCommandOutput.CertificateArn === undefined) {
    throw new Error('Failed to issue certificate');
  }

  return issueCertificateCommandOutput.CertificateArn;
}

export async function retrieveIssuedCertificate(issuedCertificateArn: string, certificateAuthorityArn: string) {
  let getCertificateCommandOutput: GetCertificateCommandOutput;
  while (true) {
    const getCertificateCommand = new GetCertificateCommand({
      CertificateArn: issuedCertificateArn,
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

  if (!getCertificateCommandOutput.Certificate) {
    throw new Error('Failed to retrieve certificate');
  }

  return getCertificateCommandOutput.Certificate;
}
