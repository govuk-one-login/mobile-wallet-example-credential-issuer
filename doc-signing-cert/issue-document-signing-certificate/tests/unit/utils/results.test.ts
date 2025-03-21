import { emptyFailure, emptySuccess, failure, success } from '../../../utils/results';

describe('emptySuccess', () => {
  it('Returns an empty success object', () => {
    expect(emptySuccess()).toEqual({
      isError: false,
    });
  });
});

describe('success', () => {
  it('Returns a success object with argument as value', () => {
    const object = { key: 'value' };
    expect(success(object)).toEqual({
      isError: false,
      value: object,
    });
  });
});

describe('emptyFailure', () => {
  it('Returns an empty failure object', () => {
    expect(emptyFailure()).toEqual({
      isError: true,
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
