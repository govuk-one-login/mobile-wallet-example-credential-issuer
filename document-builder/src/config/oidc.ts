import { AuthMiddlewareConfiguration } from "../types/AuthMiddlewareConfiguration";
import {
  getSelfUrl,
  getOIDCClientId,
  getOIDCDiscoveryEndpoint,
} from "./appConfig";
import { Client, ClientMetadata, Issuer } from "openid-client";

const SCOPES = ["openid", "wallet-subject-id"];

export function getOIDCConfig(): AuthMiddlewareConfiguration {
  return {
    clientId: getOIDCClientId(),
    discoveryEndpoint: getOIDCDiscoveryEndpoint(),
    redirectUri: getSelfUrl() + "/return-from-auth",
  } as AuthMiddlewareConfiguration;
}

async function getIssuer(discoveryUri: string) {
  return await Issuer.discover(discoveryUri);
}

export async function getOIDCClient(
  config: AuthMiddlewareConfiguration,
): Promise<Client> {
  const issuer = await getIssuer(config.discoveryEndpoint);

  const clientMetadata: ClientMetadata = {
    client_id: config.clientId,
    redirect_uris: [config.redirectUri],
    response_types: ["code"],
    token_endpoint_auth_method: "none", // required to allow custom client_assertion
    id_token_signed_response_alg: "ES256",
    scopes: SCOPES.join(" "),
  };

  return new issuer.Client(clientMetadata);
}
