import { VeteranCardRequestBody } from "../types/VeteranCardRequestBody";

export function calculateCredentialTtlSeconds(
  body: VeteranCardRequestBody,
): number {
  if (body.credentialTtl !== "other") {
    return parseInt(body.credentialTtl);
  }

  const now = new Date();
  const expiryDate = new Date(
    parseInt(body["credentialExpiry-year"]),
    parseInt(body["credentialExpiry-month"]) - 1,
    parseInt(body["credentialExpiry-day"]),
  );

  return Math.floor((expiryDate.getTime() - now.getTime()) / 1000);
}
