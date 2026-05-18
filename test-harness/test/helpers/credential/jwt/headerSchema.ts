export const headerSchema = {
  type: "object",
  properties: {
    alg: {
      type: "string",
      const: "ES256",
    },
    typ: {
      type: "string",
      const: "vc+jwt",
    },
    cty: {
      type: "string",
      const: "vc",
    },
    kid: {
      type: "string",
      pattern: String.raw`^did:web:[a-z0-9.#\-_:]+$`,
    },
  },
  additionalProperties: false,
  required: ["alg", "typ", "cty", "kid"],
};
