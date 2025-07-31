import { Context } from 'aws-lambda';
import { LogMessage } from '../logging/LogMessages';
import { logger } from '../logging/logger';

export type Dependencies = {
  env: NodeJS.ProcessEnv;
};

const dependencies: Dependencies = {
  env: process.env,
};

export const jwksHandler = lambdaHandlerConstructor(dependencies);

export function lambdaHandlerConstructor(dependencies: Dependencies) {
  return async (_event: unknown, context: Context) => {
    logger.addContext(context);
    logger.info(LogMessage.MOCK_JWKS_LAMBDA_STARTED);
    return;
  };
}
