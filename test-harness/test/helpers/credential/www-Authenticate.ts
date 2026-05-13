export function wwwAuthenticateHeaderContainsCorrectError(
  header: string,
): boolean {
  if (!header.startsWith("Bearer ")) return false;
  return /\berror="invalid_token"/.test(header);
}

export function wwwAuthenticateHeaderHasNoErrorParams(header: string): boolean {
  const hasErrorParams = /\b(error|error_description)=/.test(header);
  if (hasErrorParams) return false;
  return header === "Bearer" || header.startsWith("Bearer ");
}
