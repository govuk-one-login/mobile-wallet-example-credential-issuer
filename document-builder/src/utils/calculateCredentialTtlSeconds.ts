export function calculateCredentialTtlSeconds(
  credentialTtl: string,
  expiryDay?: string,
  expiryMonth?: string,
  expiryYear?: string,
): number {
  if (credentialTtl !== "other") {
    return parseInt(credentialTtl);
  }

  if (!expiryDay || !expiryMonth || !expiryYear) {
    throw new Error(
      "Expiry date fields are required when credentialTtl is 'other'",
    );
  }

  const now = new Date();
  const expiryDate = new Date(
    parseInt(expiryYear),
    parseInt(expiryMonth) - 1,
    parseInt(expiryDay),
  );

  return Math.floor((expiryDate.getTime() - now.getTime()) / 1000);
}
