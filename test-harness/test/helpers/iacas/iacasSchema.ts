export const iacasSchema = {
  type: "object",
  properties: {
    data: {
      type: "array",
      minItems: 1,
      items: {
        type: "object",
        properties: {
          id: {
            type: "string",
          },
          active: {
            type: "boolean",
          },
          certificatePem: {
            type: "string",
          },
          certificateData: {
            type: "object",
            properties: {
              notAfter: {
                type: "string",
              },
              notBefore: {
                type: "string",
              },
              country: {
                type: "string",
                const: "GB",
              },
              commonName: {
                type: "string",
              },
            },
            required: ["notAfter", "notBefore", "country", "commonName"],
            additionalProperties: false,
          },
          certificateFingerprint: {
            type: "string",
          },
          publicKeyJwk: {
            type: "object",
            properties: {
              kty: { type: "string", const: "EC" },
              crv: { type: "string", const: "P-256" },
              x: { type: "string" },
              y: { type: "string" },
              alg: { type: "string", const: "ES256" },
            },
            required: ["kty", "crv", "x", "y", "alg"],
            additionalProperties: true,
          },
        },
        required: [
          "id",
          "active",
          "certificatePem",
          "certificateData",
          "certificateFingerprint",
          "publicKeyJwk",
        ],
        additionalProperties: false,
      },
    },
  },
  required: ["data"],
  additionalProperties: false,
};
