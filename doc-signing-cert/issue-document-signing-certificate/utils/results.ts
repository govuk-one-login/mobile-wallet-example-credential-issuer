import { FailureWithError, SuccessWithValue } from '../types/Result';

export function success<T>(value: T): SuccessWithValue<T> {
  return {
    isError: false,
    value,
  };
}

export function failure<T>(error: T): FailureWithError<T> {
  return {
    isError: true,
    error,
  };
}
