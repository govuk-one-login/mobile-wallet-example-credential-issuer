import { EmptyFailure, EmptySuccess, FailureWithError, SuccessWithValue } from '../types/Result';

export function emptySuccess(): EmptySuccess {
  return {
    isError: false,
  };
}

export function success<T>(value: T): SuccessWithValue<T> {
  return {
    isError: false,
    value,
  };
}

export function emptyFailure(): EmptyFailure {
  return {
    isError: true,
  };
}

export function failure<T>(error: T): FailureWithError<T> {
  return {
    isError: true,
    error,
  };
}
