export interface Error {
  text: string;
  href: string;
}

export function formatValidationError(
  key: string,
  validationMessage: string,
): Record<string, Error> {
  const error: Record<string, Error> = {};
  error[key] = {
    text: validationMessage,
    href: `#${key}`,
  };
  return error;
}

export function generateErrorList(errors: Record<string, Error>) {
  const errorValues = Object.values(errors);
  return [...new Map(errorValues.map((error) => [error.text, error])).values()];
}
