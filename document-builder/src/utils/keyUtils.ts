import { createPublicKey } from "node:crypto";

export const createJwkFromRawPublicKey = (
  rawPublicKey: Uint8Array,
): JsonWebKey => {
  const stringPublicKey = uint8ArrayToBase64(rawPublicKey);
  const wrappedKey = stringPublicKey.replace(/(.{64})/g, "$1\n");

  const formattedPublicKey =
    "-----BEGIN PUBLIC KEY-----\n" + wrappedKey + "\n-----END PUBLIC KEY-----";

  return createPublicKey(formattedPublicKey).export({
    format: "jwk",
  });
};

export const uint8ArrayToBase64 = (uint8Array: Uint8Array) => {
  return Buffer.from(uint8Array).toString("base64");
};
