export function calculateCredentialTtlSeconds(
  expiryDay: string,
  expiryMonth: string,
  expiryYear: string,
): number {
  const now = new Date();
  const expiryDate = new Date(
    Date.UTC(
      Number.parseInt(expiryYear),
      Number.parseInt(expiryMonth) - 1,
      Number.parseInt(expiryDay),
    ),
  );

  return Math.floor((expiryDate.getTime() - now.getTime()) / 1000);
}
