export class MDLValidationError extends Error {
  public readonly code: string;

  constructor(message: string, code = "VALIDATION_FAILED") {
    super(message);
    this.name = "MDLValidationError";
    this.code = code;
  }
}

export function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : String(error);
}
