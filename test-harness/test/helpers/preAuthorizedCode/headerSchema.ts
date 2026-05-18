export const headerSchema = {
  type: "object",
  properties: {
    kid: {
      type: "string",
      minLength: 1,
    },
    typ: {
      type: "string",
      enum: ["JWT"],
    },
    alg: {
      type: "string",
      enum: ["ES256"],
    },
  },
  additionalProperties: false,
  required: ["kid", "typ", "alg"],
};
