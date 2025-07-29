import { LogAttributes } from '@aws-lambda-powertools/logger/types';

export class LogMessage implements LogAttributes {
  static readonly MOCK_ISSUE_LAMBDA_STARTED = new LogMessage(
    'MOCK_ISSUE_LAMBDA_STARTED',
    'Mock Issue Lambda has started.',
    'N/A',
  );
  static readonly MOCK_REVOKE_LAMBDA_STARTED = new LogMessage(
    'MOCK_REVOKE_LAMBDA_STARTED',
    'Mock Revoke Lambda has started.',
    'N/A',
  );
  static readonly MOCK_JWKS_LAMBDA_STARTED = new LogMessage(
    'MOCK_JWKS_LAMBDA_STARTED',
    'Mock JWKS Lambda has started.',
    'N/A',
  );
  private constructor(
    public readonly messageCode: string,
    public readonly message: string,
    public readonly userImpact: string,
  ) {}

  [key: string]: string;
}
