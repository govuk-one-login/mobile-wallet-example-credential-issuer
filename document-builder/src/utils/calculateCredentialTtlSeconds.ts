export function calculateCredentialTtlSeconds(
  expiryDay: string,
  expiryMonth: string,
  expiryYear: string,
): number {
  const now = new Date();
  const expiryDate = new Date(
    parseInt(expiryYear),
    parseInt(expiryMonth) - 1,
    parseInt(expiryDay),
  );

  return Math.floor((expiryDate.getTime() - now.getTime()) / 1000);
}
