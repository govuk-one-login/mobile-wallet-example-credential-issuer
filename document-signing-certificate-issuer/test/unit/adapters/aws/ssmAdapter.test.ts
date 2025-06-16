import { GetParameterCommand, SSMClient } from '@aws-sdk/client-ssm';
import { mockClient } from 'aws-sdk-client-mock';
import { getSsmParameter } from '../../../../src/adapters/aws/ssmAdapter';
import 'aws-sdk-client-mock-jest';

const mockSsmClient = mockClient(SSMClient);

describe('ssmAdapter', () => {
  beforeEach(() => {
    mockSsmClient.reset();
  });

  describe('getSsmParameter', () => {
    it('should retrieve a parameter returned from SSM', async () => {
      // ARRANGE
      mockSsmClient.on(GetParameterCommand).resolves({
        Parameter: {
          Value: 'PARAMETER_VALUE',
        },
      });

      // ACT
      const response = await getSsmParameter('PARAMETER_NAME');

      // ASSERT
      expect(response).toEqual('PARAMETER_VALUE');
      expect(mockSsmClient).toHaveReceivedCommandWith(GetParameterCommand, {
        Name: 'PARAMETER_NAME',
      });
    });

    it('should reject if the call to SSM rejects', async () => {
      // ARRANGE
      mockSsmClient.on(GetParameterCommand).rejects('REJECTED');

      // ACT
      const promise = getSsmParameter('PARAMETER_NAME');

      // ASSERT
      return expect(promise).rejects.toEqual(Error('REJECTED'));
    });

    it('should reject if the returned parameter is undefined', async () => {
      // ARRANGE
      mockSsmClient.on(GetParameterCommand).resolves({
        Parameter: {
          Value: undefined,
        },
      });

      // ACT
      const promise = getSsmParameter('PARAMETER_NAME');

      // ASSERT
      return expect(promise).rejects.toEqual(Error('Unable to retrieve parameter from SSM'));
    });
  });
});
