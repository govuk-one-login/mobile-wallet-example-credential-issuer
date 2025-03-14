import { Result } from './types/Result';
import { logger } from './logging/logger';
import { LogMessage } from './logging/LogMessages';
import { Config, getRequiredEnvironmentVariables, MissingEnvVarError } from './utils/environment';

const REQUIRED_ENVIRONMENT_VARIABLES = [
  'PLATFORM_CA_ARN_PARAMETER',
  'PLATFORM_CA_ISSUER_ALTERNATIVE_NAME',
  'DOC_SIGNING_KEY_ID',
  'DOC_SIGNING_KEY_BUCKET',
] as const;

export type IssueDocumentSigningCertificateConfig = Config<(typeof REQUIRED_ENVIRONMENT_VARIABLES)[number]>;

export function getConfigFromEnvironment(
  env: NodeJS.ProcessEnv,
): Result<IssueDocumentSigningCertificateConfig, MissingEnvVarError> {
  const envVarsResult = getRequiredEnvironmentVariables(env, REQUIRED_ENVIRONMENT_VARIABLES);
  if (envVarsResult.isError) {
    logger.error(LogMessage.APP_CHECK_INCIDENT_CHECKER_INVALID_CONFIG, {
      data: { missingEnvironmentVariables: envVarsResult.error.missingEnvVars },
    });
  }
  return envVarsResult;
}
