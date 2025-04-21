import { expect } from '@jest/globals';

/* eslint @typescript-eslint/no-explicit-any: 0 */

function isSubsetOf(object: any, targetObject: any) {
  return Object.keys(object).every((key) => deepEquals(object[key], targetObject[key]));
}

function deepEquals(subject: any, target: any): boolean {
  return JSON.stringify(subject) === JSON.stringify(target);
}

expect.extend({
  toHaveBeenCalledWithLogFields(consoleSpy, logFields) {
    const messages = consoleSpy.mock.calls.map((args: any) => args[0]);
    const pass = messages.some((message: any) => {
      const messageAsObject = JSON.parse(message);
      return isSubsetOf(logFields, messageAsObject);
    });
    return {
      pass,
      message: pass
        ? () =>
            `Expected not to find any log messages matching the specified fields and values: ${JSON.stringify(
              logFields,
            )}`
        : () =>
            `Expected to find at least one log message matching the specified fields and values: ${JSON.stringify(
              logFields,
            )}`,
    };
  },
});

declare module 'expect' {
  interface Matchers<R> {
    toHaveBeenCalledWithLogFields(logFields: object): R;
  }
}
