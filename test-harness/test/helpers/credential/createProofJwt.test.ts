import { createDidKey, createProofJwt } from "./createProofJwt";
import { decodeJwt, decodeProtectedHeader } from "jose";

describe("createProofJwt", () => {
  describe("createProofJwt", () => {
    it("should return the proof JWT", async () => {
      jest.useFakeTimers().setSystemTime(new Date("2024-07-17T12:16:00.000Z"));
      const nonce = "e4cedcf6-1fb1-48f8-bf74-94cfbe9d0d86";
      const preAuthorizedCodePayload = {
        aud: "urn:fdc:gov:uk:wallet",
        clientId: "EXAMPLE_CRI",
        iss: "urn:fdc:gov:uk:example-credential-issuer",
        credential_identifiers: ["e0b02438-d006-4100-918a-b02629e1e29c"],
        exp: 1721223394,
        iat: 1721223094,
      };
      const privateKeyJwk = {
        kty: "EC",
        x: "MMDgSI-XZWGzTCuPXwJerzvcvn93CJTe8ARsb0oLZw8",
        y: "VexEnyluTVBOrT_0ZOmNTl2ab9CXFTvb4BDIB93Mv7g",
        crv: "P-256",
        d: "K7DmYFhkGoXdwBROSL2mZvcNxONlhBQj5kV7yevigtk",
      };
      const didKey =
        "did:key:zDnaeo4ut8iyu1NUmzYN16cm3gWHp3YZWG2C6uEKeFZgEWPe7";

      const response = await createProofJwt(
        nonce,
        didKey,
        preAuthorizedCodePayload,
        privateKeyJwk,
      );
      const proofJwtPayload = decodeJwt(response);
      const proofJwtHeader = decodeProtectedHeader(response);

      expect(response).toBeTruthy();
      expect(proofJwtPayload.aud).toEqual(preAuthorizedCodePayload.iss);
      expect(proofJwtPayload.iss).toEqual(preAuthorizedCodePayload.aud);
      expect(proofJwtPayload.nonce).toEqual(nonce);
      expect(proofJwtPayload.iat).toEqual(1721218560);
      expect(proofJwtPayload.nonce).toEqual(nonce);
      expect(proofJwtHeader.kid).toEqual(
        "did:key:zDnaeo4ut8iyu1NUmzYN16cm3gWHp3YZWG2C6uEKeFZgEWPe7",
      );
      expect(proofJwtHeader.alg).toEqual("ES256");
      expect(proofJwtHeader.typ).toEqual("openid4vci-proof+jwt");
    });
  });

  describe("createDidKey", () => {
    it("should create a DID key from a JWK", async () => {
      const publicKeyJwk = {
        kty: "EC",
        x: "UFgGaSQ8drsCJ9PsvYHMRfVQjo82iCQ2RIkfe1eWzTg",
        y: "k9AO7P3HmojHqSWM5ALd_XRGlAjHIDx_o5edrr9Wdz8",
        crv: "P-256",
      };

      const response = createDidKey(publicKeyJwk);

      expect(response).toEqual(
        "did:key:zDnaeo4ut8iyu1NUmzYN16cm3gWHp3YZWG2C6uEKeFZgEWPe7",
      );
    });
  });
});
