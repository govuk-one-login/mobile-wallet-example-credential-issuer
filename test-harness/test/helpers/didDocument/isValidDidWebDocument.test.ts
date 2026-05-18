import { isValidDidWebDocument } from "./isValidDidWebDocument";

const criDomain = "example-cri.test.gov.uk";

describe("isValidDidWebDocument", () => {
  it("should return 'true' when DID document is valid", async () => {
    const didWebDocument = didWebDocumentBuilder().withDefaults();
    expect(await isValidDidWebDocument(didWebDocument, criDomain)).toEqual(
      true,
    );
  });

  it("should throw 'INVALID_DID_DOCUMENT' error when 'verificationMethod' missing from DID document", async () => {
    const didWebDocument = didWebDocumentBuilder().withOverrides({
      verificationMethod: undefined,
    });
    await expect(
      isValidDidWebDocument(didWebDocument, criDomain),
    ).rejects.toThrow(
      'INVALID_DID_DOCUMENT: DID document does not comply with the schema. [{"instancePath":"","schemaPath":"#/required","keyword":"required","params":{"missingProperty":"verificationMethod"},"message":"must have required property \'verificationMethod\'"}]',
    );
  });

  it("should throw 'INVALID_DID_DOCUMENT' error when 'id' does not match pattern", async () => {
    const didWebDocument = didWebDocumentBuilder().withOverrides({
      id: "did:web:SOMETHING-ELSE.test.gov.uk",
    });
    await expect(
      isValidDidWebDocument(didWebDocument, criDomain),
    ).rejects.toThrow(
      'INVALID_DID_DOCUMENT: Invalid "id" value in DID document. Should be did:web:example-cri.test.gov.uk but found did:web:SOMETHING-ELSE.test.gov.uk',
    );
  });

  it("should throw 'INVALID_DID_DOCUMENT' error when 'controller' does not match pattern", async () => {
    const didWebDocument = didWebDocumentBuilder().withOverrides({
      verificationMethod: [
        {
          id: "did:web:example-cri.test.gov.uk#5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f",
          type: "JsonWebKey2020",
          controller: "did:web:SOMETHING-ELSE.test.gov.uk",
          publicKeyJwk: {
            kty: "EC",
            kid: "5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f",
            crv: "P-256",
            x: "6jCKX_QRrmTeEJi-uiwcYqu8BgMgl70g2pdAst24MPE",
            y: "icPzjbSk6apD_SNvQt8NWOPlPeGG4KYU55GfnARryoY",
            alg: "ES256",
          },
        },
      ],
    });
    await expect(
      isValidDidWebDocument(didWebDocument, criDomain),
    ).rejects.toThrow(
      'INVALID_DID_DOCUMENT: Invalid "controller" value in "verificationMethod". Should be did:web:example-cri.test.gov.uk but found did:web:SOMETHING-ELSE.test.gov.uk',
    );
  });

  it("should throw 'INVALID_DID_DOCUMENT' error when 'assertionMethod' is missing an 'id'", async () => {
    const didWebDocument = didWebDocumentBuilder().withOverrides({
      assertionMethod: [
        "did:web:example-cri.test.gov.uk#5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f",
      ],
    });
    await expect(
      isValidDidWebDocument(didWebDocument, criDomain),
    ).rejects.toThrow(
      'INVALID_DID_DOCUMENT: "id" did:web:example-cri.test.gov.uk#6dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510a is missing in "assertionMethod" did:web:example-cri.test.gov.uk#5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f',
    );
  });

  it("should throw 'INVALID_DID_DOCUMENT' error when 'kid' is not in 'id'", async () => {
    const didWebDocument = didWebDocumentBuilder().withOverrides({
      verificationMethod: [
        {
          id: "did:web:example-cri.test.gov.uk#5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f",
          type: "JsonWebKey2020",
          controller: "did:web:example-cri.test.gov.uk",
          publicKeyJwk: {
            kty: "EC",
            kid: "SOMETHING-ELSE",
            crv: "P-256",
            x: "6jCKX_QRrmTeEJi-uiwcYqu8BgMgl70g2pdAst24MPE",
            y: "icPzjbSk6apD_SNvQt8NWOPlPeGG4KYU55GfnARryoY",
            alg: "ES256",
          },
        },
      ],
    });
    await expect(
      isValidDidWebDocument(didWebDocument, criDomain),
    ).rejects.toThrow(
      'INVALID_DID_DOCUMENT: Invalid "id" value in "verificationMethod". Should be did:web:example-cri.test.gov.uk#SOMETHING-ELSE but found did:web:example-cri.test.gov.uk#5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f',
    );
  });
});

function didWebDocumentBuilder<T>(): {
  withDefaults();
  withOverrides(overrides: T);
} {
  const defaults = {
    "@context": [
      "https://www.w3.org/ns/did/v1",
      "https://w3id.org/security/suites/jws-2020/v1",
    ],
    id: "did:web:example-cri.test.gov.uk",
    verificationMethod: [
      {
        id: "did:web:example-cri.test.gov.uk#5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f",
        type: "JsonWebKey2020",
        controller: "did:web:example-cri.test.gov.uk",
        publicKeyJwk: {
          kty: "EC",
          kid: "5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f",
          crv: "P-256",
          x: "6jCKX_QRrmTeEJi-uiwcYqu8BgMgl70g2pdAst24MPE",
          y: "icPzjbSk6apD_SNvQt8NWOPlPeGG4KYU55GfnARryoY",
          alg: "ES256",
        },
      },
      {
        id: "did:web:example-cri.test.gov.uk#6dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510a",
        type: "JsonWebKey2020",
        controller: "did:web:example-cri.test.gov.uk",
        publicKeyJwk: {
          kty: "EC",
          kid: "6dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510a",
          crv: "P-256",
          x: "6jCKX_QRrmTeEJi-uiwcYqu8BgMgl70g2pdAst24MPE",
          y: "icPzjbSk6apD_SNvQt8NWOPlPeGG4KYU55GfnARryoY",
          alg: "ES256",
        },
      },
    ],
    assertionMethod: [
      "did:web:example-cri.test.gov.uk#5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f",
      "did:web:example-cri.test.gov.uk#6dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510a",
    ],
  };
  return {
    withDefaults() {
      return { ...defaults };
    },
    withOverrides(overrides: T) {
      return { ...defaults, ...overrides };
    },
  };
}
