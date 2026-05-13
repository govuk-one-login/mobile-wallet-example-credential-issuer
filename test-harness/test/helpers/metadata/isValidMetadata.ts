import { metadataSchema } from "./metadataSchema";
import { getAjvInstance } from "../ajv/ajvInstance";
import { credentialConfigurationsSupportedSchema } from "./credentialConfigurationsSupportedSchema";

interface Metadata {
  credential_issuer: string;
  authorization_servers: string[];
  credential_endpoint: string;
  credential_configurations_supported: CredentialConfigurationsSupported;
  notification_endpoint?: string;
  mdoc_iacas_uri?: string;
}
type CredentialConfigurationsSupported = Record<
  string,
  CredentialConfiguration
>;

export interface CredentialConfiguration {
  format: "jwt_vc_json" | "mso_mdoc";
  cryptographic_binding_methods_supported: string[];
  credential_signing_alg_values_supported: string[];
  credential_validity_period_max_days: number;
  credential_refresh_web_journey_url: string;
  doctype?: string;
  credential_definition?: {
    type: string[];
  };
  proof_types_supported?: {
    jwt: {
      proof_signing_alg_values_supported: string[];
    };
  };
}

export interface IsValidMetadata {
  metadata: unknown;
  criUrl: string;
  authServerUrl: string;
  credentialFormat: string;
  credentialConfigurationId: string;
  hasNotificationEndpoint: boolean;
}

export async function isValidMetadata({
  metadata,
  criUrl,
  authServerUrl,
  credentialFormat,
  credentialConfigurationId,
  hasNotificationEndpoint,
}: IsValidMetadata): Promise<true> {
  const ajv = getAjvInstance();

  const validator = ajv
    .addSchema(
      credentialConfigurationsSupportedSchema,
      "credentialConfigurationsSupported",
    )
    .compile<Metadata>(metadataSchema);

  if (!validator(metadata)) {
    const validationErrors = validator.errors;
    throw new Error(
      `INVALID_METADATA: Metadata does not comply with the schema. ${JSON.stringify(validationErrors)}`,
    );
  }

  if (metadata.credential_issuer !== criUrl) {
    throw new Error(
      `INVALID_METADATA: Invalid "credential_issuer" value. Should be ${criUrl} but found ${metadata.credential_issuer}`,
    );
  }

  if (!metadata.authorization_servers.includes(authServerUrl)) {
    throw new Error(
      `INVALID_METADATA: Invalid "authorization_servers" value. Should contain ${authServerUrl} but only contains ${metadata.authorization_servers}`,
    );
  }

  const credentialEndpoint = criUrl + "/credential";
  if (metadata.credential_endpoint !== credentialEndpoint) {
    throw new Error(
      `INVALID_METADATA: Invalid "credential_endpoint" value. Should be ${credentialEndpoint} but found ${metadata.credential_endpoint}`,
    );
  }

  if (
    metadata.credential_configurations_supported[credentialConfigurationId] ===
    undefined
  ) {
    throw new Error(
      `INVALID_METADATA: Invalid "credential_configurations_supported" value. Missing credential ${credentialConfigurationId}`,
    );
  }

  if (hasNotificationEndpoint) {
    const notificationEndpoint = criUrl + "/notification";
    if (!metadata.notification_endpoint) {
      throw new Error(
        "INVALID_METADATA: Invalid metadata. Missing notification_endpoint",
      );
    }

    if (metadata.notification_endpoint !== notificationEndpoint) {
      throw new Error(
        `INVALID_METADATA: Invalid "notification_endpoint" value. Should be ${notificationEndpoint} but found ${metadata.notification_endpoint}`,
      );
    }
  }

  if (credentialFormat === "mdoc") {
    const iacasEndpoint = criUrl + "/iacas";
    if (!metadata.mdoc_iacas_uri) {
      throw new Error(
        "INVALID_METADATA: Invalid metadata. Missing mdoc_iacas_uri",
      );
    }

    if (metadata.mdoc_iacas_uri !== iacasEndpoint) {
      throw new Error(
        `INVALID_METADATA: Invalid "mdoc_iacas_uri" value. Should be ${iacasEndpoint} but found ${metadata.mdoc_iacas_uri}`,
      );
    }
  }

  return true;
}
