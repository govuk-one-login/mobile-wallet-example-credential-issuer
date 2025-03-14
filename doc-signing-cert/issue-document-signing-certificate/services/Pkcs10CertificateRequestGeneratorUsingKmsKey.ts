import {
  AlgorithmProvider,
  diAlgorithmProvider,
  diAsnSignatureFormatter,
  HashedAlgorithm,
  IAsnSignatureFormatter,
  Name,
  Pkcs10CertificateRequest,
  Pkcs10CertificateRequestCreateParams,
} from '@peculiar/x509';
import { GetPublicKeyCommand, KMSClient, SignCommand } from '@aws-sdk/client-kms';
import { CertificationRequest, CertificationRequestInfo } from '@peculiar/asn1-csr';
import { AsnConvert } from '@peculiar/asn1-schema';
import {
  Attribute as AsnAttribute,
  Extension as AsnExtension,
  Extensions,
  Name as AsnName,
  SubjectPublicKeyInfo,
} from '@peculiar/asn1-x509';
import { id_pkcs9_at_extensionRequest } from '@peculiar/asn1-pkcs9';
import { container } from 'tsyringe';
import { Buffer } from 'buffer';

export class Pkcs10CertificateRequestGeneratorUsingKmsKey {
  public static async create(
    params: Partial<Pkcs10CertificateRequestCreateParams>,
    kmsId: string,
    kmsClient: KMSClient,
  ) {
    const getPublicKeyCommand = new GetPublicKeyCommand({
      KeyId: kmsId,
    });
    const getPublicKeyCommandOutput = await kmsClient.send(getPublicKeyCommand);
    const spki = getPublicKeyCommandOutput.PublicKey;
    if (spki === undefined) {
      throw Error('Error retrieving public key from KMS');
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
      SigningAlgorithm: 'ECDSA_SHA_256',
    });
    const signCommandOutput = await kmsClient.send(signCommand);
    const signature = signCommandOutput.Signature;
    if (signature === undefined) {
      throw Error('An error occured when signing the request with KMS');
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
      throw Error('Cannot convert WebCrypto signature value to ASN.1 format');
    }

    asnReq.signature = asnSignature;

    return new Pkcs10CertificateRequest(AsnConvert.serialize(asnReq));
  }
}
