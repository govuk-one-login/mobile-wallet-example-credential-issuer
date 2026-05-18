export const credentialConfigurationsSupportedSchema = {
  $id: "credential-configurations-supported",
  type: "object",
  patternProperties: {
    "^.*$": {
      type: "object",
      additionalProperties: true,
      properties: {
        format: {
          type: "string",
          enum: ["jwt_vc_json", "mso_mdoc"],
        },
        cryptographic_binding_methods_supported: {
          type: "array",
          items: { type: "string" },
          minItems: 1,
        },
        credential_signing_alg_values_supported: {
          type: "array",
          items: { type: "string" },
          contains: { const: "ES256" },
          minItems: 1,
        },
        credential_validity_period_max_days: {
          type: "number",
        },
        credential_refresh_web_journey_url: {
          type: "string",
          format: "uri",
        },
      },
      required: [
        "format",
        "cryptographic_binding_methods_supported",
        "credential_signing_alg_values_supported",
        "credential_validity_period_max_days",
        "credential_refresh_web_journey_url",
      ],
      if: {
        properties: { format: { const: "mso_mdoc" } },
      },
      then: {
        required: ["doctype"],
        properties: {
          doctype: { type: "string" },
          cryptographic_binding_methods_supported: {
            type: "array",
            contains: { const: "cose_key" },
          },
        },
      },
      else: {
        if: {
          properties: { format: { const: "jwt_vc_json" } },
        },
        then: {
          required: ["credential_definition", "proof_types_supported"],
          properties: {
            credential_definition: {
              type: "object",
              additionalProperties: true,
              properties: {
                type: {
                  type: "array",
                  items: { type: "string" },
                  minItems: 2,
                  maxItems: 2,
                  contains: { const: "VerifiableCredential" },
                },
              },
              required: ["type"],
            },
            proof_types_supported: {
              type: "object",
              additionalProperties: true,
              properties: {
                jwt: {
                  type: "object",
                  additionalProperties: true,
                  properties: {
                    proof_signing_alg_values_supported: {
                      type: "array",
                      items: { type: "string" },
                      contains: { const: "ES256" },
                      minItems: 1,
                    },
                  },
                  required: ["proof_signing_alg_values_supported"],
                },
              },
              required: ["jwt"],
            },
            cryptographic_binding_methods_supported: {
              type: "array",
              contains: { const: "did:key" },
            },
          },
        },
      },
    },
  },
};
