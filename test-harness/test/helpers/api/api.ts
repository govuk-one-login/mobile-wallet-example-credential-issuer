import axios, { AxiosResponse } from "axios";

async function get(criUrl: string, path: string): Promise<AxiosResponse> {
  try {
    const url = new URL(path, criUrl).toString();
    return await axios.get(getDockerDnsName(url));
  } catch (error) {
    throw new Error(
      `API_ERROR: Error trying to fetch ${path} - ${JSON.stringify(error)}`,
    );
  }
}

export async function getJwks(criUrl: string): Promise<AxiosResponse> {
  return get(criUrl, ".well-known/jwks.json");
}

export async function getMetadata(criUrl: string): Promise<AxiosResponse> {
  return get(criUrl, ".well-known/openid-credential-issuer");
}

export async function getDidDocument(criUrl: string): Promise<AxiosResponse> {
  return get(criUrl, ".well-known/did.json");
}

export async function getIacas(criUrl: string, path): Promise<AxiosResponse> {
  return get(criUrl, path);
}

export async function getCredential(
  accessToken: string,
  proofJwt: string,
  credentialUrl: string,
): Promise<AxiosResponse> {
  return await axios.post(
    getDockerDnsName(credentialUrl),
    {
      proof: {
        proof_type: "jwt",
        jwt: proofJwt,
      },
    },
    {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
    },
  );
}

export async function sendNotification(
  accessToken: string | undefined,
  notification_id: string | undefined,
  event: string,
  notificationUrl: string,
): Promise<AxiosResponse> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(accessToken && { Authorization: `Bearer ${accessToken}` }),
  };

  return await axios.post(
    getDockerDnsName(notificationUrl),
    {
      notification_id,
      event,
    },
    { headers },
  );
}

// When running locally, "localhost" must be replaced with "host.docker.internal" when making a request
export function getDockerDnsName(url) {
  if (url.startsWith("http://localhost")) {
    return url.replace("localhost", "host.docker.internal");
  } else {
    return url;
  }
}
