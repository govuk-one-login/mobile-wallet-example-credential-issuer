import {
  AlgorithmProvider,
  AsnEcSignatureFormatter,
  Name,
  Pkcs10CertificateRequest,
  Pkcs10CertificateRequestCreateParamsName,
  X509Certificate,
} from "@peculiar/x509";
import { CertificationRequest, CertificationRequestInfo } from '@peculiar/asn1-csr';
import { AsnConvert } from '@peculiar/asn1-schema';
import { Name as AsnName, SubjectPublicKeyInfo } from '@peculiar/asn1-x509';
import { signWithEcdsaSha256, getPublicKey } from '../aws/kmsAdapter';

export async function createCertificateRequestFromEs256KmsKey(
  csrName: Pkcs10CertificateRequestCreateParamsName,
  kmsId: string,
) {
  const spki = await getPublicKey(kmsId);

  const asnReq = new CertificationRequest({
    certificationRequestInfo: new CertificationRequestInfo({
      subjectPKInfo: AsnConvert.parse(spki, SubjectPublicKeyInfo),
    }),
  });

  const name = csrName instanceof Name ? csrName : new Name(csrName);
  asnReq.certificationRequestInfo.subject = AsnConvert.parse(name.toArrayBuffer(), AsnName);

  const signingAlgorithm = {
    name: 'ECDSA',
    namedCurve: 'P-256',
    hash: 'SHA-256',
  };

  const algProv = new AlgorithmProvider();
  asnReq.signatureAlgorithm = algProv.toAsnAlgorithm(signingAlgorithm);

  const tbs = AsnConvert.serialize(asnReq.certificationRequestInfo);
  const signature = await signWithEcdsaSha256(kmsId, tbs);

  const signatureFormatter = new AsnEcSignatureFormatter();
  const asnSignature = signatureFormatter.toAsnSignature(signingAlgorithm, signature);

  if (!asnSignature) {
    throw Error('Cannot convert WebCrypto signature value to ASN.1 format');
  }

  asnReq.signature = asnSignature;

  return new Pkcs10CertificateRequest(AsnConvert.serialize(asnReq));
}

export function decodeX509Certificate(certificatePem: string): X509Certificate {
  return new X509Certificate(certificatePem);
}
