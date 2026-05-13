import { isValidCredential } from "./isValidCredential";
import * as createProofJwtModule from "../createProofJwt";
import { importJWK, SignJWT } from "jose";

jest.mock("../createProofJwt", () => ({
  createProofJwt: jest.fn(),
  createDidKey: jest.fn(),
}));
jest.mock("../createAccessToken", () => ({
  createAccessToken: jest.fn(),
}));

const criUrl = "https://test-example-cri.gov.uk";
const kid =
  "did:web:test-example-cri.gov.uk#78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274";
const verificationMethod = [
  {
    id: "did:web:test-example-cri.gov.uk#78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274",
    type: "JsonWebKey2020",
    controller: "did:web:test-example-cri.gov.uk",
    publicKeyJwk: {
      alg: "ES256",
      kid: "78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274",
      kty: "EC",
      x: "-OxU7o3ZtHJ7GnufJkGKv3EAgeisXdZg1eTKErzsiL8",
      y: "1yKvdIgdktb6MYaVU2Ptt_yrnU1Y5gmT2uJbc9q4vGg",
      crv: "P-256",
    },
  },
];
const didKey = "did:key:zDnaecAXbW1Z3Gr8D8W1XXysV4XRWDMZGWPLGiCupHBjehR6c";

describe("isValidCredential", () => {
  const createDidKey = createProofJwtModule.createDidKey as jest.Mock;

  beforeEach(() => {
    jest.useFakeTimers().setSystemTime(new Date("2024-08-01T09:08:24.000Z"));
    createDidKey.mockReturnValue(didKey);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it("should return 'true' when credential is valid", async () => {
    const credential = await getTestJwt(criUrl, kid, didKey);

    expect(
      await isValidCredential(credential, didKey, verificationMethod, criUrl),
    ).toEqual(true);
  });

  it("should throw 'HEADER_DECODING_ERROR' error when token header cannot be decoded", async () => {
    const credential =
      "invalidHeader" + (await getTestJwt(criUrl, kid, didKey));

    await expect(
      isValidCredential(credential, didKey, verificationMethod, criUrl),
    ).rejects.toThrow(
      "INVALID_HEADER: Failed to decode credential header. TypeError: Invalid Token or Protected Header formatting",
    );
  });

  it("should throw 'INVALID_HEADER' error when header is missing 'kid' claim", async () => {
    const credential = await getTestJwt(criUrl, undefined, didKey);

    await expect(
      isValidCredential(credential, didKey, verificationMethod, criUrl),
    ).rejects.toThrow(
      'INVALID_HEADER: Credential header does not comply with the schema. [{"instancePath":"","schemaPath":"#/required","keyword":"required","params":{"missingProperty":"kid"},"message":"must have required property \'kid\'"}]',
    );
  });

  it("should throw 'PUBLIC_KEY_NOT_IN_DID' error when when public key is not in the DID document", async () => {
    const credential = await getTestJwt(
      criUrl,
      "did:web:test-example-cri.gov.uk#11fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032059645",
      didKey,
    );
    const verificationMethod = [
      {
        id: "did:web:test-example-cri.gov.uk#78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274",
        type: "JsonWebKey2020",
        controller: "did:web:test-example-cri.gov.uk",
        publicKeyJwk: {
          alg: "ES256",
          kid: "78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274",
          kty: "EC",
          x: "oU5Xs7sFXCckKMKGAiRMhv1q7RWqlYTl80Voqi1kZow",
          y: "mXADd0XOLEtq8mk2mP0qhdDnS0hIUjQJZ4fJ1Df3Cvo",
          crv: "P-256",
        },
      },
    ];

    await expect(
      isValidCredential(credential, didKey, verificationMethod, criUrl),
    ).rejects.toThrow(
      "INVALID_SIGNATURE: No public key found in DID for provided 'kid'",
    );
  });

  it("should throw 'INVALID_SIGNATURE' when signature cannot be verified", async () => {
    const credential = await getTestJwt(criUrl, kid, didKey);
    const verificationMethod = [
      {
        id: "did:web:test-example-cri.gov.uk#78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274",
        type: "JsonWebKey2020",
        controller: "did:web:test-example-cri.gov.uk",
        publicKeyJwk: {
          alg: "ES256",
          kid: "78fa131d677c1ac0f172c53b47ac169a95ad0d92c38bd794a70da59032058274",
          kty: "EC",
          x: "oU5Xs7sFXCckKMKGAiRMhv1q7RWqlYTl80Voqi1kZow",
          y: "mXADd0XOLEtq8mk2mP0qhdDnS0hIUjQJZ4fJ1Df3Cvo",
          crv: "P-256",
        },
      },
    ];

    await expect(
      isValidCredential(credential, didKey, verificationMethod, criUrl),
    ).rejects.toThrow(
      'INVALID_SIGNATURE: Credential verification failed. {"code":"ERR_JWS_SIGNATURE_VERIFICATION_FAILED","name":"JWSSignatureVerificationFailed"}',
    );
  });

  it("should throw 'INVALID_PAYLOAD' error when payload is missing 'iss' claim", async () => {
    const credential = await getTestJwt(undefined, kid, didKey);

    await expect(
      isValidCredential(credential, didKey, verificationMethod, criUrl),
    ).rejects.toThrow(
      'INVALID_PAYLOAD: Credential payload does not comply with the schema. [{"instancePath":"","schemaPath":"#/required","keyword":"required","params":{"missingProperty":"iss"},"message":"must have required property \'iss\'"}]',
    );
  });

  it("should throw 'INVALID_PAYLOAD' error when 'iss' claim value is not the CRI URL", async () => {
    const credential = await getTestJwt("invalidIssuer", kid, didKey);

    await expect(
      isValidCredential(credential, didKey, verificationMethod, criUrl),
    ).rejects.toThrow(
      'INVALID_PAYLOAD: Invalid "iss" value in token. Should be https://test-example-cri.gov.uk but found "invalidIssuer"',
    );
  });

  it("should throw 'INVALID_PAYLOAD' error when 'sub' claim value does not match the Proof JWT 'did:key' value", async () => {
    const credential = await getTestJwt(criUrl, kid, "notTheProofJwtDidKey");

    await expect(
      isValidCredential(credential, didKey, verificationMethod, criUrl),
    ).rejects.toThrow(
      'INVALID_PAYLOAD: Invalid "sub" value in token. Should be did:key:zDnaecAXbW1Z3Gr8D8W1XXysV4XRWDMZGWPLGiCupHBjehR6c but found "notTheProofJwtDidKey"',
    );
  });
});

async function getTestJwt(issuer, kid, sub) {
  const privateKey = {
    kty: "EC",
    x: "-OxU7o3ZtHJ7GnufJkGKv3EAgeisXdZg1eTKErzsiL8",
    y: "1yKvdIgdktb6MYaVU2Ptt_yrnU1Y5gmT2uJbc9q4vGg",
    crv: "P-256",
    d: "uhF3qwj2ddRwnWO84tCS-qJEsm7m__bAG5x6klw-rng",
  };
  const signingKeyAsKeyLike = await importJWK(privateKey, "ES256");

  return await new SignJWT({
    iss: "https://test-example-cri.gov.uk",
    sub: sub,
    nbf: 1721731169,
    exp: 1754060904,
    "@context": [
      "https://www.w3.org/ns/credentials/v2",
      "https://www.w3.org/ns/credentials/examples/v2",
    ],
    type: ["VerifiableCredential", "digitalVeteranCard"],
    issuer: "https://test-example-cri.gov.uk",
    name: "Veteran's Card",
    description: "issuer-specified credential description",
    validFrom: "2024-04-09T12:12:11Z",
    validUntil: "2034-04-08T22:59:59Z",
    credentialSubject: { id: sub },
  })
    .setProtectedHeader({ alg: "ES256", typ: "vc+jwt", cty: "vc", kid: kid })
    .setIssuedAt(1721731169)
    .setExpirationTime("1year")
    .setIssuer(issuer)
    .setNotBefore(1721731169)
    .sign(signingKeyAsKeyLike);
}
