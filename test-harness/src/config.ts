/**
 * Configuration module for environment variables and application settings
 */

const ENV_VARS = {
  PORT: "PORT",
  CREDENTIAL_OFFER_DEEP_LINK: "CREDENTIAL_OFFER_DEEP_LINK",
  CRI_URL: "CRI_URL",
  WALLET_SUBJECT_ID: "WALLET_SUBJECT_ID",
  TEST_HARNESS_URL: "TEST_HARNESS_URL",
  CLIENT_ID: "CLIENT_ID",
  CREDENTIAL_FORMAT: "CREDENTIAL_FORMAT",
  HAS_NOTIFICATION_ENDPOINT: "HAS_NOTIFICATION_ENDPOINT",
} as const;

/**
 * Gets an environment variable value
 * @param variableName - Name of the environment variable
 * @returns The environment variable value
 * @throws Error if the environment variable is not set
 */
function getEnvVarValue(variableName: string): string {
  const variableValue = process.env[variableName];
  if (!variableValue) {
    throw new Error(`${variableName} environment variable not set`);
  }
  return variableValue;
}

export function getPortNumber(): number {
  return Number.parseInt(getEnvVarValue(ENV_VARS.PORT), 10);
}

export function getCredentialOfferDeepLink(): string {
  return getEnvVarValue(ENV_VARS.CREDENTIAL_OFFER_DEEP_LINK);
}

export function getWalletSubjectId(): string {
  return getEnvVarValue(ENV_VARS.WALLET_SUBJECT_ID);
}

export function getKeyId(): string {
  return "5d76b492-d62e-46f4-a3d9-bc51e8b91ac5";
}

export function getCriUrl(): string {
  return getEnvVarValue(ENV_VARS.CRI_URL);
}

export function getSelfURL(): string {
  return getEnvVarValue(ENV_VARS.TEST_HARNESS_URL);
}

export function getClientId(): string {
  return getEnvVarValue(ENV_VARS.CLIENT_ID);
}

export function getHasNotificationEndpoint(): string {
  return getEnvVarValue(ENV_VARS.HAS_NOTIFICATION_ENDPOINT);
}

export function getCredentialFormat(): "jwt" | "mdoc" {
  const credentialFormat = getEnvVarValue(ENV_VARS.CREDENTIAL_FORMAT);
  if (credentialFormat !== "jwt" && credentialFormat !== "mdoc") {
    throw new Error(
      `Invalid credential format: ${credentialFormat}. Must be 'jwt' or 'mdoc'.`,
    );
  }
  return credentialFormat;
}
