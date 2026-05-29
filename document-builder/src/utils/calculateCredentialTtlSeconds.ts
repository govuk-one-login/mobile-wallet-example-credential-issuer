export function calculateCredentialTtlSeconds(
  expiryDay: string,
  expiryMonth: string,
  expiryYear: string,
): number {
  const now = new Date();
  now.setUTCHours(0, 0, 0, 0);

  const expiryDate = new Date(
    Date.UTC(
      Number.parseInt(expiryYear),
      Number.parseInt(expiryMonth) - 1, // Date.UTC months are 0-indexed
      Number.parseInt(expiryDay),
    ),
  );

  return Math.floor((expiryDate.getTime() - now.getTime()) / 1000);
}
