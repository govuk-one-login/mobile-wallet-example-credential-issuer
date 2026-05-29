export interface ValidationResult {
  isValid: boolean;
  errors: Record<string, string>;
}
