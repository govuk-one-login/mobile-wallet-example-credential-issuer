import { importJWK, JWK, JWTPayload, SignJWT } from "jose";
import bs58 from "bs58";

const SIGNING_ALGORITHM = "ES256";
const PROOF_JWT_ISSUER = "urn:fdc:gov:uk:wallet";
const TYPE = "openid4vci-proof+jwt";

export async function createProofJwt(
  nonce: string,
  didKey: string,
  preAuthorizedCodePayload: JWTPayload,
  privateKeyJwk: JWK,
): Promise<string> {
  const signingKeyAsKeyLike = await importJWK(privateKeyJwk, SIGNING_ALGORITHM);

  return await new SignJWT({ nonce: nonce })
    .setProtectedHeader({ alg: SIGNING_ALGORITHM, kid: didKey, typ: TYPE })
    .setIssuedAt()
    .setIssuer(PROOF_JWT_ISSUER)
    .setAudience(preAuthorizedCodePayload.iss!)
    .sign(signingKeyAsKeyLike);
}

export function createDidKey(publicKeyJwk: JWK): string {
  const publicKeyBuffer = getPublicKeyFromJwk(publicKeyJwk);
  const compressedPublicKey = compress(publicKeyBuffer);

  const bytes = new Uint8Array(compressedPublicKey.length + 2);
  bytes[0] = 0x80;
  bytes[1] = 0x24;
  bytes.set(compressedPublicKey, 2);

  const base58EncodedKey = bs58.encode(bytes);
  return `did:key:z${base58EncodedKey}`;
}

function getPublicKeyFromJwk(publicKeyJwk: JWK) {
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
  compressedKey[0] = 2 + ((y.at(-1) ?? 0) & 1);

  compressedKey.set(x, 1);
  return compressedKey;
}
