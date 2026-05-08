import { decode as decodeCbor, getEncoded, Tag } from "cbor2";
import { Sign1 } from "@auth0/cose";
import { X509Certificate } from "node:crypto";
import { base64UrlDecoder } from "../../utils/base64Encoder";
import { logger } from "../../middleware/logger";
import { CredentialData } from "../types";

let cborTagsRegistered = false;

/**
 * Registers CBOR tag decoders for nested CBOR structures.
 * Call this before decoding mDoc credentials.
 * Safe to call multiple times - only registers once.
 */
export function registerCborTagDecoders(): void {
  if (cborTagsRegistered) return;

  // Tag 24: bstr values that are themselves encoded CBOR
  Tag.registerDecoder(24, ({ contents }) => {
    return decodeCbor(contents as Buffer);
  });

  cborTagsRegistered = true;
}

/**
 * Decodes an mdoc credential from CBOR format
 * @param credential - Base64URL encoded CBOR credential
 * @returns Object containing decoded claims, signature, and payload
 */
export function decodeMDocCredential(credential: string): {
  credentialClaims: unknown;
  credentialSignature: Sign1;
  credentialSignaturePayload: unknown;
} {
  registerCborTagDecoders();

  const credentialClaims = decodeCbor(base64UrlDecoder(credential), {
    saveOriginal: true,
  });
  // @ts-expect-error credential structure is known
  const rawMso = credentialClaims.issuerAuth;
  const credentialSignature = Sign1.decode(getEncoded(rawMso)!);
  const credentialSignaturePayload = decodeCbor(
    Buffer.from(credentialSignature.payload),
  );

  return {
    credentialClaims,
    credentialSignature,
    credentialSignaturePayload,
  };
}

/**
 * Extracts and decodes X.509 certificate chain from credential signature
 * @param credentialSignature - COSE Sign1 signature containing x5chain
 * @returns Object with certificate chain in PEM format and hex encoding
 */
export function decodeX5Chain(credentialSignature: Sign1): {
  x5chain: string;
  x5chainHex: string;
} {
  // Element 33 in the UnprotectedHeaders map is the x5chain.
  // There must be at least one certificate. If there is more then this is an array of certificates
  const x5chainBuffer = credentialSignature.unprotectedHeaders.get(33);

  const x5chainHex = (x5chainBuffer as Buffer).toString("hex");
  let x5chain = "";
  let x5chainCerts: Buffer[];
  if (Array.isArray(x5chainBuffer)) {
    x5chainCerts = x5chainBuffer;
  } else {
    x5chainCerts = [x5chainBuffer as Buffer];
  }
  x5chainCerts.forEach((certificate) => {
    const x5cert = new X509Certificate(certificate);
    x5chain = x5cert.toString() + "\n";
  });
  return {
    x5chain,
    x5chainHex,
  };
}

/**
 * Decodes a credential as CBOR (mdoc format)
 * @param credential - The credential string to decode
 * @returns Credential data with CBOR-specific properties
 */
export function decodeCredentialAsCbor(credential: string): CredentialData {
  try {
    const {
      credentialClaims,
      credentialSignature,
      credentialSignaturePayload,
    } = decodeMDocCredential(credential);

    let x5chain = "";
    let x5chainHex = "";
    try {
      ({ x5chain, x5chainHex } = decodeX5Chain(credentialSignature));
    } catch (error) {
      x5chain = "An error occurred decoding the x5chain element in the MSO";
      logger.info(
        error,
        "An error occurred decoding the x5chain element in the MSO",
      );
    }

    logger.info("Decoded CBOR credential");
    return {
      credentialClaims,
      credentialSignature,
      credentialSignaturePayload,
      credentialClaimsTitle: "mdoc Credential",
      x5chain,
      x5chainHex,
    };
  } catch (error) {
    logger.error(error, "An error occurred whilst decoding a CBOR credential");
    return {
      credentialClaims: undefined,
      credentialClaimsTitle: "mdoc Credential",
      x5chain: "",
      x5chainHex: "",
    };
  }
}
