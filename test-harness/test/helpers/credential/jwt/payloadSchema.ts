export const payloadSchema = {
  type: "object",
  properties: {
    iss: {
      type: "string",
      minLength: 1,
    },
    sub: {
      type: "string",
      minLength: 1,
    },
    nbf: {
      type: "number",
    },
    iat: {
      type: "number",
    },
    exp: {
      type: "number",
    },
    "@context": {
      type: "array",
      minItems: 1,
      uniqueItems: true,
      items: {
        type: "string",
      },
      contains: {
        const: "https://www.w3.org/ns/credentials/v2",
      },
    },
    type: {
      type: "array",
      minItems: 1,
      maxItems: 2,
      uniqueItems: true,
      items: {
        type: "string",
        minLength: 1,
      },
      contains: {
        const: "VerifiableCredential",
      },
    },
    issuer: {
      type: "string",
      minLength: 1,
    },
    name: {
      type: "string",
      minLength: 1,
    },
    description: {
      type: "string",
      minLength: 1,
    },
    validFrom: {
      type: "string",
      pattern: String.raw`^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d)|3[01]T([01]\d|2[0-3]):[0-5]\d:[0-5]\dZ$`,
    },
    validUntil: {
      type: "string",
      pattern: String.raw`^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d)|3[01]T([01]\d|2[0-3]):[0-5]\d:[0-5]\dZ$`,
    },
    credentialSubject: {
      type: "object",
      properties: {
        id: {
          type: "string",
          minLength: 1,
        },
      },
      additionalProperties: true,
      required: ["id"],
    },
  },
  additionalProperties: true,
  required: [
    "iss",
    "sub",
    "@context",
    "type",
    "issuer",
    "validUntil",
    "credentialSubject",
  ],
};
