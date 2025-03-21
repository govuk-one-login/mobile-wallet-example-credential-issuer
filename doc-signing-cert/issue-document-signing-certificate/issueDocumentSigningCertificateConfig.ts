import { Result } from './types/Result';
import { Config, getRequiredEnvironmentVariables, MissingEnvVarError } from './utils/environment';

const REQUIRED_ENVIRONMENT_VARIABLES = [
  'PLATFORM_CA_ARN_PARAMETER',
  'PLATFORM_CA_ISSUER_ALTERNATIVE_NAME',
  'DOC_SIGNING_KEY_ID',
  'DOC_SIGNING_KEY_BUCKET',
  'DOC_SIGNING_KEY_VALIDITY_PERIOD',
  'DOC_SIGNING_KEY_COMMON_NAME',
  'DOC_SIGNING_KEY_COUNTRY_NAME',
] as const;

export type IssueDocumentSigningCertificateConfig = Config<(typeof REQUIRED_ENVIRONMENT_VARIABLES)[number]>;

export function getConfigFromEnvironment(
  env: NodeJS.ProcessEnv,
): Result<IssueDocumentSigningCertificateConfig, MissingEnvVarError> {
  return getRequiredEnvironmentVariables(env, REQUIRED_ENVIRONMENT_VARIABLES);
}
