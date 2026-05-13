import { createHash } from "node:crypto";
import { iacasSchema } from "./iacasSchema";
import { X509Certificate } from "@peculiar/x509";
import { getAjvInstance } from "../ajv/ajvInstance";

export interface CertificateData {
  notAfter: string;
  notBefore: string;
  country: string;
  commonName: string;
}

export interface PublicKeyJwk {
  kty: string;
  crv: string;
  x: string;
  y: string;
  alg: string;
}

export interface CertificateItem {
  id: string;
  active: boolean;
  certificatePem: string;
  certificateData: CertificateData;
  certificateFingerprint: string;
  publicKeyJwk: PublicKeyJwk;
}

export interface Iacas {
  data: CertificateItem[];
}

export async function isValidIacas(iacas: Iacas): Promise<boolean> {
  const ajv = getAjvInstance();
  const rulesValidator = ajv.compile(iacasSchema);
  if (!rulesValidator(iacas)) {
    throw new Error(
      `INVALID_IACAS: Schema validation failed. ${JSON.stringify(rulesValidator.errors)}`,
    );
  }

  const iaca = iacas.data[0];
  const pem = iaca.certificatePem
    .replace("-----BEGIN CERTIFICATE-----", "")
    .replace("-----END CERTIFICATE-----", "")
    .replaceAll(/\s+/g, "");

  let certificate: X509Certificate;
  try {
    certificate = new X509Certificate(pem);
  } catch {
    throw new Error(
      "INVALID_IACAS: Certificate PEM could not be parsed as X509",
    );
  }

  const { notBefore, notAfter, country, commonName } = iaca.certificateData;
  if (notBefore !== certificate.notBefore.toISOString()) {
    throw new Error(
      `INVALID_IACAS: notBefore does not match. Should be "${notBefore}" but found "${certificate.notBefore.toISOString()}"`,
    );
  }
  if (notAfter !== certificate.notAfter.toISOString()) {
    throw new Error(
      `INVALID_IACAS: notAfter does not match. Should be "${notAfter}" but found "${certificate.notAfter.toISOString()}"`,
    );
  }
  const countryField = certificate.subjectName.getField("C");
  if (countryField?.[0] !== country) {
    throw new Error(
      `INVALID_IACAS: country does not match. Should be "${country}" but found "${countryField?.[0]}"`,
    );
  }
  const commonNameField = certificate.subjectName.getField("CN");
  if (commonNameField?.[0] !== commonName) {
    throw new Error(
      `INVALID_IACAS: commonName does not match. Should be "${commonName}" but found "${commonNameField?.[0]}"`,
    );
  }

  const fingerprint = createHash("sha256")
    .update(Buffer.from(certificate.rawData))
    .digest("hex");
  if (iaca.certificateFingerprint !== fingerprint) {
    throw new Error(
      `INVALID_IACAS: Fingerprint does not match. Should be "${iaca.certificateFingerprint}" but found "${fingerprint}"`,
    );
  }

  /*
  A self-signed X.509 certificate is one where the certificate's
  issuer and subject are the same, and it is signed using its own
  private key.

  To verify the authenticity of such a certificate, its embedded
  public key must be able to verify its own digital signature.
  */
  if (!(await certificate.isSelfSigned())) {
    throw new Error("INVALID_IACAS: Certificate is not self-signed");
  }

  const jwk = { ...iaca.publicKeyJwk, key_ops: ["verify"] };
  const publicKey = await crypto.subtle.importKey(
    "jwk",
    jwk,
    { name: "ECDSA", namedCurve: jwk.crv },
    false,
    ["verify"],
  );

  if (!(await certificate.verify({ publicKey }))) {
    throw new Error(
      "INVALID_IACAS: Signature verification failed with provided JWK",
    );
  }

  return true;
}
