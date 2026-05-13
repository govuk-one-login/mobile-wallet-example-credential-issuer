import { isValidJwks } from "./isValidJwks";

describe("isValidJwks", () => {
  it("should return 'true' when JWKS is valid", async () => {
    const jwks = jwksBuilder().withDefaults();
    expect(await isValidJwks(jwks)).toEqual(true);
  });

  it("should throw 'INVALID_JWKS' error when JWKS does not contain any keys", async () => {
    const jwks = jwksBuilder().withOverrides({ keys: [] });

    await expect(isValidJwks(jwks)).rejects.toThrow(
      'INVALID_JWKS: JWKS does not comply with the schema. [{"instancePath":"/keys","schemaPath":"#/properties/keys/minItems","keyword":"minItems","params":{"limit":1},"message":"must NOT have fewer than 1 items"}]',
    );
  });

  it("should throw 'INVALID_JWKS' error when JWK is missing 'kid'", async () => {
    const jwks = jwksBuilder().withOverrides({
      keys: [
        {
          kty: "EC",
          crv: "P-256",
          x: "6jCKX_QRrmTeEJi-uiwcYqu8BgMgl70g2pdAst24MPE",
          y: "icPzjbSk6apD_SNvQt8NWOPlPeGG4KYU55GfnARryoY",
          alg: "ES256",
          use: "sig",
        },
      ],
    });

    await expect(isValidJwks(jwks)).rejects.toThrow(
      'INVALID_JWKS: JWKS does not comply with the schema. [{"instancePath":"/keys/0","schemaPath":"#/properties/keys/items/required","keyword":"required","params":{"missingProperty":"kid"},"message":"must have required property \'kid\'"}]',
    );
  });

  it("should throw 'INVALID_JWKS' error when JWK 'alg' is not 'ES256'", async () => {
    const jwks = jwksBuilder().withOverrides({
      keys: [
        {
          kty: "EC",
          kid: "5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f",
          crv: "P-256",
          x: "6jCKX_QRrmTeEJi-uiwcYqu8BgMgl70g2pdAst24MPE",
          y: "icPzjbSk6apD_SNvQt8NWOPlPeGG4KYU55GfnARryoY",
          alg: "INVALID_ALG",
          use: "sig",
        },
      ],
    });

    await expect(isValidJwks(jwks)).rejects.toThrow(
      'INVALID_JWKS: JWKS does not comply with the schema. [{"instancePath":"/keys/0/alg","schemaPath":"#/properties/keys/items/properties/alg/const","keyword":"const","params":{"allowedValue":"ES256"},"message":"must be equal to constant"}]',
    );
  });
});

function jwksBuilder<T>() {
  const jwk = {
    kty: "EC",
    kid: "5dcbee863b5d7cc30c9ba1f7393dacc6c16610782e4b6a191f94a7e8b1e1510f",
    crv: "P-256",
    x: "6jCKX_QRrmTeEJi-uiwcYqu8BgMgl70g2pdAst24MPE",
    y: "icPzjbSk6apD_SNvQt8NWOPlPeGG4KYU55GfnARryoY",
    alg: "ES256",
    use: "sig",
  };

  const defaults = {
    keys: [jwk],
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
