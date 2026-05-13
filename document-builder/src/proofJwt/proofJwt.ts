import {
  GetPublicKeyCommand,
  GetPublicKeyResponse,
  KMSClient,
  SignCommand,
  SignCommandOutput,
  SigningAlgorithmSpec,
} from "@aws-sdk/client-kms";
import { getKmsConfig } from "../config/aws";
import format from "ecdsa-sig-formatter";
import { createPublicKey } from "node:crypto";

import bs58 from "bs58";

const PROOF_TOKEN_SIGNING_ALGORITHM = "ES256";
const PROOF_TOKEN_JWT_TYPE = "openid4vci-proof+jwt";

export async function getProofJwt(
  nonce: string,
  audience: string,
  keyId: string,
): Promise<string> {
  const kmsService = new ProofJwtKmsService(keyId);
  const publicKeyRaw = await kmsService.getPublicKey();
  const publicKeyJwk = createJwkFromRawPublicKey(publicKeyRaw);
  const didKey = createDidKey(publicKeyJwk);

  const header = {
    alg: PROOF_TOKEN_SIGNING_ALGORITHM,
    typ: PROOF_TOKEN_JWT_TYPE,
    kid: didKey,
  };
  const encodedHeader = base64Encoder(header);
  const payload = {
    iss: "urn:fdc:gov:uk:wallet",
    aud: audience,
    iat: Math.floor(Date.now() / 1000),
    nonce: nonce,
  };
  const encodedPayload = base64Encoder(payload);
  const message = `${encodedHeader}.${encodedPayload}`;

  const signature = await kmsService.sign(message);

  return `${encodedHeader}.${encodedPayload}.${signature}`;
}

function base64Encoder(object: object) {
  return Buffer.from(JSON.stringify(object)).toString("base64url");
}

export class ProofJwtKmsService {
  constructor(
    private readonly keyId: string,
    private readonly signingAlgorithm: SigningAlgorithmSpec = "ECDSA_SHA_256",
    private readonly kmsClient: KMSClient = new KMSClient(getKmsConfig()),
  ) {}

  async sign(message: string): Promise<string> {
    const command: SignCommand = new SignCommand({
      Message: Buffer.from(message),
      KeyId: this.keyId,
      SigningAlgorithm: this.signingAlgorithm,
      MessageType: "RAW",
    });

    const response: SignCommandOutput = await this.kmsClient.send(command);
    const base64EncodedSignature = Buffer.from(response.Signature!).toString(
      "base64url",
    );

    return format.derToJose(base64EncodedSignature, "ES256");
  }

  public async getPublicKey() {
    const command: GetPublicKeyCommand = new GetPublicKeyCommand({
      KeyId: this.keyId,
    });

    const response: GetPublicKeyResponse = await this.kmsClient.send(command);
    return response.PublicKey!;
  }
}

export function createDidKey(publicKeyJwk: JsonWebKey): string {
  const publicKeyBuffer = getPublicKeyFromJwk(publicKeyJwk);
  const compressedPublicKey = compress(publicKeyBuffer);

  const bytes = new Uint8Array(compressedPublicKey.length + 2);
  bytes[0] = 0x80;
  bytes[1] = 0x24;
  bytes.set(compressedPublicKey, 2);

  const base58EncodedKey = bs58.encode(bytes);
  return `did:key:z${base58EncodedKey}`;
}

function getPublicKeyFromJwk(publicKeyJwk: JsonWebKey) {
  return Buffer.concat([
    Buffer.from(publicKeyJwk.x!, "base64"),
    Buffer.from(publicKeyJwk.y!, "base64"),
  ]);
}

const compress = (publicKey: Uint8Array): Uint8Array => {
  const publicKeyHex = Buffer.from(publicKey).toString("hex");
  const xHex = publicKeyHex.slice(0, publicKeyHex.length / 2);
  const yHex = publicKeyHex.slice(publicKeyHex.length / 2, publicKeyHex.length);
  const xOctet = Uint8Array.from(Buffer.from(xHex, "hex"));
  const yOctet = Uint8Array.from(Buffer.from(yHex, "hex"));
  return compressEcPoint(xOctet, yOctet);
};

function compressEcPoint(x: Uint8Array, y: Uint8Array) {
  const compressedKey = new Uint8Array(x.length + 1);
  /*
  Suppressing this code smell because 'array.at(-1)' is only supported starting from ES2022, whereas this
  project currently targets ES2016. We plan to address this issue when transitioning to ES modules and
  updating the ECMAScript target version in https://govukverify.atlassian.net/browse/DCMAW-11776.
  */
  compressedKey[0] = 2 + (y[y.length - 1] & 1); // NOSONAR

  compressedKey.set(x, 1);
  return compressedKey;
}

export const createJwkFromRawPublicKey = (
  rawPublicKey: Uint8Array,
): JsonWebKey => {
  const stringPublicKey = uint8ArrayToBase64(rawPublicKey);

  const formattedPublicKey =
    "-----BEGIN PUBLIC KEY-----\n" +
    stringPublicKey +
    "\n-----END PUBLIC KEY-----";

  return createPublicKey(formattedPublicKey).export({
    format: "jwk",
  });
};

export const uint8ArrayToBase64 = (uint8Array: Uint8Array) => {
  return Buffer.from(uint8Array).toString("base64");
};
