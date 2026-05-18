export const jwksSchema = {
  type: "object",
  properties: {
    keys: {
      type: "array",
      minItems: 1,
      items: {
        type: "object",
        properties: {
          kty: { type: "string", const: "EC" },
          crv: { type: "string", const: "P-256" },
          x: { type: "string" },
          y: { type: "string" },
          kid: { type: "string" },
          use: { type: "string" },
          alg: { type: "string", const: "ES256" },
        },
        required: ["kty", "crv", "x", "y", "kid", "use", "alg"],
        additionalProperties: false,
      },
    },
  },
  required: ["keys"],
  additionalProperties: false,
};
