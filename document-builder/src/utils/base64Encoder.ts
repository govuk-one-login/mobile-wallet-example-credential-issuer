export function base64Encoder(object: object) {
  return Buffer.from(JSON.stringify(object)).toString("base64url");
}

export function base64UrlDecoder(base64url: string) {
  const base64 =
    base64url.replaceAll("-", "+").replaceAll("_", "/") +
    "==".slice(0, (4 - (base64url.length % 4)) % 4);

  return Buffer.from(base64, "base64");
}
