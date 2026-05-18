export const payloadSchema = {
  type: "object",
  properties: {
    aud: {
      type: "string",
      minLength: 1,
    },
    clientId: {
      type: "string",
      minLength: 1,
    },
    iss: {
      type: "string",
      minLength: 1,
    },
    credential_identifiers: {
      type: "array",
      items: { type: "string" },
      minItems: 1,
      maxItems: 1,
    },
    iat: {
      type: "number",
    },
    exp: {
      type: "number",
    },
  },
  additionalProperties: false,
  required: ["aud", "clientId", "iss", "credential_identifiers", "iat", "exp"],
};
