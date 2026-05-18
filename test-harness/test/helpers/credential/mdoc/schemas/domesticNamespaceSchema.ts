export const domesticNamespaceSchema = {
  $id: "domestic-namespace",
  type: "array",
  contains: {
    type: "object",
    properties: {
      elementIdentifier: { const: "welsh_licence" },
    },
    required: ["elementIdentifier"],
  },
  items: {
    type: "object",
    required: ["digestID", "elementIdentifier", "random", "elementValue"],
    additionalProperties: false,
    properties: {
      digestID: {
        type: "integer",
        minimum: 0,
        maximum: 2147483648,
      },
      random: {
        type: "object",
        instanceofUint8Array: true,
      },
      elementIdentifier: {
        type: "string",
        enum: ["title", "welsh_licence", "provisional_driving_privileges"],
      },
      elementValue: {
        anyOf: [{ type: "boolean" }, { type: "string" }, { type: "array" }],
      },
    },
    allOf: [
      {
        if: {
          properties: {
            elementIdentifier: { const: "welsh_licence" },
          },
        },
        then: {
          properties: { elementValue: { type: "boolean" } },
        },
      },
      {
        if: {
          properties: {
            elementIdentifier: { const: "title" },
          },
        },
        then: {
          properties: { elementValue: { type: "string" } },
        },
      },
      {
        if: {
          properties: {
            elementIdentifier: { const: "provisional_driving_privileges" },
          },
        },
        then: {
          properties: {
            elementValue: {
              type: "array",
              items: {
                type: "object",
                required: ["vehicle_category_code"],
                properties: {
                  vehicle_category_code: { type: "string" },
                  issue_date: {
                    type: "string",
                    pattern: String.raw`^\d{4}-\d{2}-\d{2}$`,
                  },
                  expiry_date: {
                    type: "string",
                    pattern: String.raw`^\d{4}-\d{2}-\d{2}$`,
                  },
                  codes: {
                    type: "array",
                    items: {
                      type: "object",
                      required: ["code"],
                      properties: {
                        code: {
                          type: "string",
                        },
                        sign: {
                          type: "string",
                        },
                        value: {
                          type: "string",
                        },
                      },
                      additionalProperties: false,
                    },
                  },
                },
                additionalProperties: false,
              },
            },
          },
        },
      },
    ],
  },
};
