import axios from "axios";

const REVOKE_PATH = "/revoke";

/**
 * Revokes a document by sending a POST request to the CRI revoke endpoint.
 *
 * @param criUrl - The base URL of the Credential Issuer.
 * @param documentId - The unique identifier of the document to revoke.
 * @returns A promise that resolves to the HTTP status code of the response.
 */
export async function revoke(
  criUrl: string,
  documentId: string,
): Promise<number> {
  const revokeUrl = criUrl + REVOKE_PATH + "/" + documentId;

  const response = await axios.post(revokeUrl, null, {
    validateStatus: () => true,
  });

  return response.status;
}
