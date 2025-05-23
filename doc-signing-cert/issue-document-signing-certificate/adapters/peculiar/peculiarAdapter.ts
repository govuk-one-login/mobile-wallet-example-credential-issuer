import { AlgorithmProvider, AsnEcSignatureFormatter, Name, Pkcs10CertificateRequest } from '@peculiar/x509';
import { CertificationRequest, CertificationRequestInfo } from '@peculiar/asn1-csr';
import { AsnConvert } from '@peculiar/asn1-schema';
import { Name as AsnName, SubjectPublicKeyInfo } from '@peculiar/asn1-x509';
import { signWithEcdsaSha256, getPublicKey } from '../aws/kmsAdapter';

export async function createCertificateRequestFromEs256KmsKey(commonName: string, countryName: string, keyId: string) {
  const signingAlgorithm = {
    name: 'ECDSA',
    namedCurve: 'P-256',
    hash: 'SHA-256',
  };

  const spki = await getPublicKey(keyId);
  const name = new Name([{ CN: [commonName] }, { C: [countryName] }]);
  const certificationRequestInfo = new CertificationRequestInfo({
    subjectPKInfo: AsnConvert.parse(spki, SubjectPublicKeyInfo),
    subject: AsnConvert.parse(name.toArrayBuffer(), AsnName),
  });
  const toBeSigned = AsnConvert.serialize(certificationRequestInfo);
  const signature = await signWithEcdsaSha256(keyId, toBeSigned);
  const asnSignature = new AsnEcSignatureFormatter().toAsnSignature(signingAlgorithm, signature);
  if (!asnSignature) {
    throw new Error('Cannot convert WebCrypto signature value to ASN.1 format');
  }
  const asnRequest = new CertificationRequest({
    certificationRequestInfo,
    signatureAlgorithm: new AlgorithmProvider().toAsnAlgorithm(signingAlgorithm),
    signature: asnSignature,
  });
  return new Pkcs10CertificateRequest(AsnConvert.serialize(asnRequest)).toString();
}
