import {
  getCredentialFormat,
  getHasNotificationEndpoint,
} from "../../src/config";

/**
 * Boolean conditions for running tests.
 */
export const isJwt = () => getCredentialFormat() === "jwt";
export const isMdoc = () => getCredentialFormat() === "mdoc";
export const hasNotificationEndpoint = () =>
  getHasNotificationEndpoint() === "true";

/**
 * Version of Jest's describe() that runs based on a boolean condition.
 *
 * @param name - Test suite name
 * @param condition - Function that returns boolean
 * @param fn - Test suite function
 */
export const describeIf = (
  name: string,
  condition: () => boolean,
  fn: () => void,
) => {
  return condition() ? describe(name, fn) : describe.skip(name, fn);
};

/**
 * Version of Jest's it() that runs based on a boolean condition.
 *
 * @param name - Test name
 * @param condition - Function that returns boolean
 * @param fn - Test function
 */
export const itIf = (
  name: string,
  condition: () => boolean,
  fn: () => void,
) => {
  return condition() ? it(name, fn) : it.skip(name, fn);
};
