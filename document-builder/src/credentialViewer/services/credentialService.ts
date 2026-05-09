import axios from "axios";
import {
  getCriEndpoint,
  getOneLoginAuthServerUrl,
  getSelfUrl,
} from "../../config/appConfig";
import { GrantType } from "../../stsStubAccessToken/token/validateTokenRequest";

/**
 * Exchanges pre-authorized code for access token
 * @param preAuthorizedCode - Pre-authorized code from credential offer
 * @returns Access token string
 */
export async function getAccessToken(
  preAuthorizedCode: string,
): Promise<string> {
  const response = await axios.post(
    `${getOneLoginAuthServerUrl()}/token`,
    {
      grant_type: GrantType.PREAUTHORIZED_CODE,
      "pre-authorized_code": preAuthorizedCode,
    },
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
    },
  );
  return response.data.access_token;
}

/**
 * Generates a proof JWT for credential request
 * @param c_nonce - Challenge nonce from access token
 * @param audience - Audience for the proof JWT
 * @returns Proof JWT string
 */
export async function getProofJwt(
  c_nonce: string,
  audience: string,
): Promise<string> {
  const proofJwtResponse = await axios.get(
    `${getSelfUrl()}/proof-jwt?nonce=${c_nonce}&audience=${audience}`,
  );
  return proofJwtResponse.data.proofJwt;
}

/**
 * Requests credential from credential issuer
 * @param accessToken - Access token for authorization
 * @param proofJwt - Proof JWT for credential request
 * @returns Credential string
 */
export async function getCredential(
  accessToken: string,
  proofJwt: string,
): Promise<string> {
  const criUrl = getCriEndpoint();
  const credentialUrl = criUrl + "/credential";

  const response = await axios.post(
    credentialUrl,
    {
      proof: {
        proof_type: "jwt",
        jwt: proofJwt,
      },
    },
    {
      headers: {
        Authorization: `BEARER ${accessToken}`,
      },
    },
  );
  return response.data.credentials[0].credential;
}
