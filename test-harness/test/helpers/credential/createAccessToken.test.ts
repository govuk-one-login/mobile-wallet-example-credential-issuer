import { createAccessToken } from "./createAccessToken";
import { decodeJwt, decodeProtectedHeader } from "jose";
import crypto from "crypto";

const c_nonce = "e4cedcf6-1fb1-48f8-bf74-94cfbe9d0d86";
const walletSubjectId = "wallet_subject_id";
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
const accessTokenTtlInSeconds = 180;

describe("createAccessToken", () => {
  const mockTimestamp = 1234567890123;
  const mockedJti = "11c70ee6-d3d9-42f5-8232-548385fb58a0";

  beforeEach(() => {
    jest.clearAllMocks();
    jest.spyOn(Date, "now").mockReturnValue(mockTimestamp);
    jest.spyOn(crypto, "randomUUID").mockReturnValue(mockedJti);
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it("should return the access token", async () => {
    const response = await createAccessToken(
      c_nonce,
      walletSubjectId,
      preAuthorizedCodePayload,
      privateKeyJwk,
    );

    const accessTokenPayload = decodeJwt(response.access_token);
    const accessTokenHeader = decodeProtectedHeader(response.access_token);

    expect(response.token_type).toEqual("bearer");
    expect(response.expires_in).toEqual(accessTokenTtlInSeconds);
    expect(response.access_token).toBeTruthy();

    const requiredPayloadClaims = [
      "sub",
      "aud",
      "iss",
      "c_nonce",
      "credential_identifiers",
      "exp",
      "jti",
    ];
    requiredPayloadClaims.forEach((claim) => {
      expect(accessTokenPayload).toHaveProperty(claim);
    });
    expect(accessTokenPayload.sub).toEqual(walletSubjectId);
    expect(accessTokenPayload.aud).toEqual(preAuthorizedCodePayload.iss);
    expect(accessTokenPayload.iss).toEqual(preAuthorizedCodePayload.aud);
    expect(accessTokenPayload.c_nonce).toEqual(c_nonce);
    expect(accessTokenPayload.credential_identifiers).toEqual(
      preAuthorizedCodePayload.credential_identifiers,
    );
    expect(accessTokenPayload.jti).toEqual(mockedJti);
    const mockedNowInSeconds = Math.floor(Date.now() / 1000);
    expect(accessTokenPayload.exp).toEqual(
      mockedNowInSeconds + accessTokenTtlInSeconds,
    );

    const requiredHeaderClaims = ["kid", "typ", "alg"];
    requiredHeaderClaims.forEach((claim) => {
      expect(accessTokenHeader).toHaveProperty(claim);
    });
    expect(accessTokenHeader.kid).toEqual(
      "5d76b492-d62e-46f4-a3d9-bc51e8b91ac5",
    );
    expect(accessTokenHeader.typ).toEqual("at+jwt");
    expect(accessTokenHeader.alg).toEqual("ES256");
  });
});
