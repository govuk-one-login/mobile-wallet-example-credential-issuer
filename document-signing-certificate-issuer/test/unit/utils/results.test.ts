import { failure, success } from '../../../src/utils/results';

describe('success', () => {
  it('Returns a success object with argument as value', () => {
    const object = { key: 'value' };
    expect(success(object)).toEqual({
      isError: false,
      value: object,
    });
  });
});

describe('failure', () => {
  it('Returns a failure object with argument as error', () => {
    const object = { error: 'error' };
    expect(failure(object)).toEqual({
      isError: true,
      error: object,
    });
  });
});
