export const metadataSchema = {
  $id: "metadata",
  type: "object",
  properties: {
    credential_issuer: {
      type: "string",
      format: "uri",
    },
    authorization_servers: {
      type: "array",
      items: {
        type: "string",
        format: "uri",
      },
      minItems: 1,
    },
    credential_endpoint: {
      type: "string",
      format: "uri",
    },
    credential_configurations_supported: {
      $ref: "credential-configurations-supported",
    },
    notification_endpoint: {
      type: "string",
      format: "uri",
    },
    mdoc_iacas_uri: {
      type: "string",
      format: "uri",
    },
  },
  additionalProperties: true,
  required: [
    "credential_issuer",
    "authorization_servers",
    "credential_endpoint",
    "credential_configurations_supported",
  ],
};
