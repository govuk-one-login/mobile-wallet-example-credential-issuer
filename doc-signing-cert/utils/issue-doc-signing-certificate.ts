import { AsnConvert } from "@peculiar/asn1-schema";
import { SubjectPublicKeyInfo, Name as AsnName, Attribute as AsnAttribute, Extensions, Extension as AsnExtension } from "@peculiar/asn1-x509";
import { Buffer } from "buffer";
import * as x509 from "@peculiar/x509";
import { container } from "tsyringe";
import { GetParameterCommand, SSMClient } from "@aws-sdk/client-ssm";
import {
  ACMPCAClient,
  GetCertificateCommand,
  GetCertificateCommandOutput,
  IssueCertificateCommand,
  RequestInProgressException,
} from "@aws-sdk/client-acm-pca";
import {
  AlgorithmProvider,
  diAlgorithmProvider,
  diAsnSignatureFormatter,
  HashedAlgorithm,
  IAsnSignatureFormatter,
  Name,
  Pkcs10CertificateRequest,
  Pkcs10CertificateRequestCreateParams,
} from "@peculiar/x509";
import { GetPublicKeyCommand, KMSClient, SignCommand } from "@aws-sdk/client-kms";
import { CertificationRequest, CertificationRequestInfo } from "@peculiar/asn1-csr";
import { id_pkcs9_at_extensionRequest } from "@peculiar/asn1-pkcs9";

const ssmClient = new SSMClient();
const pcaClient = new ACMPCAClient();
const kmsClient = new KMSClient();

export class Pkcs10CertificateRequestGeneratorUsingKmsKey {
  public static async create(params: Partial<Pkcs10CertificateRequestCreateParams>, kmsId: string) {
    const getPublicKeyCommand = new GetPublicKeyCommand({
      KeyId: kmsId,
    });
    const getPublicKeyCommandOutput = await kmsClient.send(getPublicKeyCommand);
    const spki = getPublicKeyCommandOutput.PublicKey;
    if (spki === undefined) {
      throw Error("Error retrieving public key from KMS");
    }

    const asnReq = new CertificationRequest({
      certificationRequestInfo: new CertificationRequestInfo({
        subjectPKInfo: AsnConvert.parse(spki, SubjectPublicKeyInfo),
      }),
    });
    if (params.name) {
      const name = params.name instanceof Name ? params.name : new Name(params.name);
      asnReq.certificationRequestInfo.subject = AsnConvert.parse(name.toArrayBuffer(), AsnName);
    }

    // if (params.attributes) {
    //   // Add attributes
    //   for (const o of params.attributes) {
    //     asnReq.certificationRequestInfo.attributes.push(AsnConvert.parse(o.rawData, AsnAttribute));
    //   }
    // }

    // Add extensions
    if (params.extensions && params.extensions.length) {
      const attr = new AsnAttribute({ type: id_pkcs9_at_extensionRequest });
      const extensions = new Extensions();
      for (const o of params.extensions) {
        extensions.push(AsnConvert.parse(o.rawData, AsnExtension));
      }
      attr.values.push(AsnConvert.serialize(extensions));
      asnReq.certificationRequestInfo.attributes.push(attr);
    }

    // Set signing algorithm
    const signingAlgorithm = { ...params.signingAlgorithm } as HashedAlgorithm;
    const algProv = container.resolve<AlgorithmProvider>(diAlgorithmProvider);
    asnReq.signatureAlgorithm = algProv.toAsnAlgorithm(signingAlgorithm);

    // Sign
    const tbs = AsnConvert.serialize(asnReq.certificationRequestInfo);
    const signCommand = new SignCommand({
      KeyId: kmsId,
      Message: Buffer.from(tbs),
      SigningAlgorithm: "ECDSA_SHA_256",
    });
    const signCommandOutput = await kmsClient.send(signCommand);
    const signature = signCommandOutput.Signature;
    if (signature === undefined) {
      throw Error("An error occured when signing the request with KMS");
    }

    // Convert WebCrypto signature to ASN.1 format
    const signatureFormatters = container.resolveAll<IAsnSignatureFormatter>(diAsnSignatureFormatter).reverse();
    let asnSignature: ArrayBuffer | null = null;
    for (const signatureFormatter of signatureFormatters) {
      asnSignature = signatureFormatter.toAsnSignature(signingAlgorithm, signature);
      if (asnSignature) {
        break;
      }
    }
    if (!asnSignature) {
      throw Error("Cannot convert WebCrypto signature value to ASN.1 format");
    }

    asnReq.signature = asnSignature;

    return new Pkcs10CertificateRequest(AsnConvert.serialize(asnReq));
  }
}

async function getSsmParameter(parameterName: string) {
  const getParameterCommandOutput = await ssmClient.send(
    new GetParameterCommand({
      Name: parameterName,
    }),
  );
  return getParameterCommandOutput.Parameter?.Value;
}

async function issueDocSigningCertificate() {
  const certificateAuthorityArn = await getSsmParameter("/platform-ca/iaca/certificate-authority-arn");
  const issuerAlternativeName = await getSsmParameter("/platform-ca/iaca/issuer-alternative-name");
  const rootCertificate = await getSsmParameter("/platform-ca/iaca/root-certificate");
  const rootCertificateArn = await getSsmParameter("/platform-ca/iaca/root-certificate-arn");
  const kmsKeyArn = "arn:aws:kms:eu-west-2:671524980203:key/d6e6c61d-3dc8-4686-8a39-ecdd6f147eb5";
  const kmsKeyId = "d6e6c61d-3dc8-4686-8a39-ecdd6f147eb5";

  // Generate CSR
  const alg = {
    name: "ECDSA",
    namedCurve: "P-256",
    hash: "SHA-256",
  };
  const csr = await Pkcs10CertificateRequestGeneratorUsingKmsKey.create(
    {
      name: [{ CN: ["Test Certificate"] }, { C: ["UK"] }],
      signingAlgorithm: alg,
      extensions: [new x509.KeyUsagesExtension(x509.KeyUsageFlags.digitalSignature | x509.KeyUsageFlags.keyEncipherment)],
    },
    kmsKeyId,
  );

  // Send Request to AWS Private CA to issue Certificate
  const issueCertificateCommand = new IssueCertificateCommand({
    ApiPassthrough: {
      Extensions: {
        KeyUsage: {
          DigitalSignature: true,
        },
        ExtendedKeyUsage: [
          {
            ExtendedKeyUsageObjectIdentifier: "1.0.18013.5.1.2", // identifier for ISO mDL
          },
        ],
        CustomExtensions: [
          {
            ObjectIdentifier: "2.5.29.18",
            Value: issuerAlternativeName,
          },
        ],
      },
    },
    CertificateAuthorityArn: certificateAuthorityArn,
    Csr: Buffer.from(csr.toString()),
    SigningAlgorithm: "SHA256WITHECDSA",
    TemplateArn: "arn:aws:acm-pca:::template/BlankEndEntityCertificate_APIPassthrough/V1",
    Validity: {
      Value: 1825,
      Type: "DAYS",
    },
  });

  const issueCertificateCommandOutput = await pcaClient.send(issueCertificateCommand);

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

  console.log(getCertificateCommandOutput.Certificate);
}

issueDocSigningCertificate()
  .then(() => {
    process.exit(0);
  })
  .catch((err) => {
    console.error("An error occurred:", err);
    process.exit(1);
  });
