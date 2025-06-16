import { GetParameterCommand, SSMClient } from '@aws-sdk/client-ssm';

const ssmClient = new SSMClient();

export async function getSsmParameter(parameterName: string) {
  const getParameterCommandOutput = await ssmClient.send(
    new GetParameterCommand({
      Name: parameterName,
    }),
  );

  if (!getParameterCommandOutput.Parameter?.Value) {
    throw new Error('Unable to retrieve parameter from SSM');
  }

  return getParameterCommandOutput.Parameter?.Value;
}
