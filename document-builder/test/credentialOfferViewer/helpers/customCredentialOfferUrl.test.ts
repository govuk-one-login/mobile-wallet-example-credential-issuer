import { customiseCredentialOfferUrl } from "../../../src/credentialOfferViewer/helpers/customCredentialOfferUrl";
import { WalletAppsConfig } from "../../../src/config/walletAppsConfig";

const walletAppsConfig: WalletAppsConfig = {
  "test-app-1": {
    url: "https://test-one.com/wallet/",
    name: "Test App (1)",
  },
  "test-app-2": {
    url: "https://test-two.com/wallet/",
    name: "Test App (2)",
  },
  "test-app-3": {
    url: "https://test-three.com/wallet/",
    name: "Test App (3)",
  },
};

const walletApps = ["test-app-1", "test-app-3"];

describe("customiseCredentialOfferUrl", () => {
  it('should return the URL for the "test-app-1"', async () => {
    const credentialOfferUrl =
      "https://mobile.account.gov.uk/wallet/add?credential_offer=%7B%22credentials%22%3A%5B%22BasicCheckCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiMzAzM2VmNjctOGYwOS00MmQyLThhYTQtMmFlZDFhMTU2ZGZmIl0sImV4cCI6MTcxNTYwODU4MywiaWF0IjoxNzE1NjA4MjgzfQ.5n8xVRaOR1H5E7EVkApCwigBNChxTEvMfWCr2KTolKzzqTHdDnJRtprI1rfrqB85DvCqYYYSdsoku6SmZXoHUw%22%7D%7D%2C%22credentialIssuer%22%3A%22http%3A%2F%2Flocalhost%3A8080%22%7D";
    const selectedApp = "test-app-1";
    const error = "";

    const response = customiseCredentialOfferUrl(
      credentialOfferUrl,
      selectedApp,
      walletAppsConfig,
      walletApps,
      error,
    );

    expect(response).toEqual(
      "https://test-one.com/wallet/add?credential_offer=%7B%22credentials%22%3A%5B%22BasicCheckCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiMzAzM2VmNjctOGYwOS00MmQyLThhYTQtMmFlZDFhMTU2ZGZmIl0sImV4cCI6MTcxNTYwODU4MywiaWF0IjoxNzE1NjA4MjgzfQ.5n8xVRaOR1H5E7EVkApCwigBNChxTEvMfWCr2KTolKzzqTHdDnJRtprI1rfrqB85DvCqYYYSdsoku6SmZXoHUw%22%7D%7D%2C%22credentialIssuer%22%3A%22http%3A%2F%2Flocalhost%3A8080%22%7D",
    );
  });

  it("should throw an error if the URL returned by the CRI is invalid", async () => {
    const credentialOfferUrl =
      "https://not.the.expected.url.com/wallet/add?credential_offer=%7B%22credentials%22%3A%5B%22BasicCheckCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiMzAzM2VmNjctOGYwOS00MmQyLThhYTQtMmFlZDFhMTU2ZGZmIl0sImV4cCI6MTcxNTYwODU4MywiaWF0IjoxNzE1NjA4MjgzfQ.5n8xVRaOR1H5E7EVkApCwigBNChxTEvMfWCr2KTolKzzqTHdDnJRtprI1rfrqB85DvCqYYYSdsoku6SmZXoHUw%22%7D%7D%2C%22credentialIssuer%22%3A%22http%3A%2F%2Flocalhost%3A8080%22%7D";
    const selectedApp = "test-app-1";
    const error = "";

    expect(() => {
      customiseCredentialOfferUrl(
        credentialOfferUrl,
        selectedApp,
        walletAppsConfig,
        walletApps,
        error,
      );
    }).toThrow("Invalid URL");
  });

  it("should not replace the pre-authorized code when error is falsy", async () => {
    const credentialOfferUrl =
      "https://mobile.account.gov.uk/wallet/add?credential_offer=%7B%22credentials%22%3A%5B%22BasicCheckCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiMzAzM2VmNjctOGYwOS00MmQyLThhYTQtMmFlZDFhMTU2ZGZmIl0sImV4cCI6MTcxNTYwODU4MywiaWF0IjoxNzE1NjA4MjgzfQ.5n8xVRaOR1H5E7EVkApCwigBNChxTEvMfWCr2KTolKzzqTHdDnJRtprI1rfrqB85DvCqYYYSdsoku6SmZXoHUw%22%7D%7D%2C%22credentialIssuer%22%3A%22http%3A%2F%2Flocalhost%3A8080%22%7D";
    const selectedApp = "test-app-1";
    const error = undefined;

    const response = customiseCredentialOfferUrl(
      credentialOfferUrl,
      selectedApp,
      walletAppsConfig,
      walletApps,
      error,
    );

    expect(response).toEqual(
      "https://test-one.com/wallet/add?credential_offer=%7B%22credentials%22%3A%5B%22BasicCheckCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiMzAzM2VmNjctOGYwOS00MmQyLThhYTQtMmFlZDFhMTU2ZGZmIl0sImV4cCI6MTcxNTYwODU4MywiaWF0IjoxNzE1NjA4MjgzfQ.5n8xVRaOR1H5E7EVkApCwigBNChxTEvMfWCr2KTolKzzqTHdDnJRtprI1rfrqB85DvCqYYYSdsoku6SmZXoHUw%22%7D%7D%2C%22credentialIssuer%22%3A%22http%3A%2F%2Flocalhost%3A8080%22%7D",
    );
  });

  it("should replace the pre-authorized code with 'ERROR:500' when error=ERROR:500", async () => {
    const credentialOfferUrl =
      "https://mobile.account.gov.uk/wallet/add?credential_offer=%7B%22credentials%22%3A%5B%22BasicCheckCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiMzAzM2VmNjctOGYwOS00MmQyLThhYTQtMmFlZDFhMTU2ZGZmIl0sImV4cCI6MTcxNTYwODU4MywiaWF0IjoxNzE1NjA4MjgzfQ.5n8xVRaOR1H5E7EVkApCwigBNChxTEvMfWCr2KTolKzzqTHdDnJRtprI1rfrqB85DvCqYYYSdsoku6SmZXoHUw%22%7D%7D%2C%22credentialIssuer%22%3A%22http%3A%2F%2Flocalhost%3A8080%22%7D";
    const selectedApp = "test-app-1";
    const error = "ERROR:500";
    const response = customiseCredentialOfferUrl(
      credentialOfferUrl,
      selectedApp,
      walletAppsConfig,
      walletApps,
      error,
    );

    expect(response).toEqual(
      "https://test-one.com/wallet/add?credential_offer=%7B%22credentials%22%3A%5B%22BasicCheckCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22ERROR%3A500%22%7D%7D%2C%22credentialIssuer%22%3A%22http%3A%2F%2Flocalhost%3A8080%22%7D",
    );
  });

  it("should throw an error if the credential offer in the URL returned by the CRI does not match the expected pattern", async () => {
    const credentialOfferUrl =
      "https://mobile.account.gov.uk/wallet/add?credential_broken_offer=%7B%22credentials%22%3A%5B%22BasicCheckCredential%22%5D%2C%22grants%22%3A%7B%22urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Apre-authorized_code%22%3A%7B%22pre-authorized_code%22%3A%22eyJraWQiOiJmZjI3NWI5Mi0wZGVmLTRkZmMtYjBmNi04N2M5NmIyNmM2YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJhdWQiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJjbGllbnRJZCI6IkVYQU1QTEVfQ1JJIiwiaXNzIjoidXJuOmZkYzpnb3Y6dWs6ZXhhbXBsZS1jcmVkZW50aWFsLWlzc3VlciIsImNyZWRlbnRpYWxfaWRlbnRpZmllcnMiOlsiMzAzM2VmNjctOGYwOS00MmQyLThhYTQtMmFlZDFhMTU2ZGZmIl0sImV4cCI6MTcxNTYwODU4MywiaWF0IjoxNzE1NjA4MjgzfQ.5n8xVRaOR1H5E7EVkApCwigBNChxTEvMfWCr2KTolKzzqTHdDnJRtprI1rfrqB85DvCqYYYSdsoku6SmZXoHUw%22%7D%7D%2C%22credentialIssuer%22%3A%22http%3A%2F%2Flocalhost%3A8080%22%7D";
    const selectedApp = "test-app-1";
    const error = "ERROR:500";

    expect(() => {
      customiseCredentialOfferUrl(
        credentialOfferUrl,
        selectedApp,

        walletAppsConfig,
        walletApps,
        error,
      );
    }).toThrow("Invalid URL");
  });
});
