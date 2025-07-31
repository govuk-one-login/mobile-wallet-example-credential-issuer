/* eslint @typescript-eslint/no-invalid-void-type: 0 */

export type Result<T, E> = Success<T> | Failure<E>;

export type Success<T> = T extends void ? EmptySuccess : SuccessWithValue<T>;

export type EmptySuccess = {
  isError: false;
};

export type SuccessWithValue<T> = {
  isError: false;
  value: T;
};

export type Failure<E = BaseError> = E extends void ? EmptyFailure : FailureWithError<E>;

export type EmptyFailure = {
  isError: true;
};

export type FailureWithError<E = BaseError> = {
  isError: true;
  error: E;
};

export interface BaseError {
  error: string;
  errorDescription: string;
}
