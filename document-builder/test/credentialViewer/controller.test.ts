import { MDOC_TEST_DATA, VCDM_TEST_DATA } from "./testData";

process.env.SELF = "https://doc-builder.test";
process.env.CREDENTIAL_ISSUER_URL = "https://example-cri.test";
process.env.ONE_LOGIN_AUTH_SERVER_URL = "https://sts-mock.test";

import { credentialViewerController } from "../../src/credentialViewer/controller";
import { getMockReq, getMockRes } from "@jest-mock/express";
import axios, { AxiosResponse } from "axios";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("controller.ts", () => {
  const userinfo = {
    wallet_subject_id:
      "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
  };
  const req = getMockReq({
    cookies: {
      app: "some-staging-app",
      id_token: "id_token",
      access_token: "access_token",
    },
    query: {
      offer:
        'https://mobile.dev.account.gov.uk/wallet-test/add?credential_offer={"credentials":["SocialSecurityCredential"],"grants":{"urn:ietf:params:oauth:grant-type:pre-authorized_code":{"pre-authorized_code":"eyJraWQiOiI3OGZhMTMxZDY3N2MxYWMwZjE3MmM1M2I0N2FjMTY5YTk1YWQwZDkyYzM4YmQ3OTRhNzBkYTU5MDMyMDU4Mjc0IiwidHlwIjoiSldUIiwiYWxnIjoiRVMyNTYifQ.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwMDEiLCJjbGllbnRJZCI6IlRFU1RfQ0xJRU5UX0lEIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiY3JlZGVudGlhbF9pZGVudGlmaWVycyI6WyIzNjg0ZWM2ZC05MmRkLTQyYTgtYmY0Yy1mYTIwMGE4M2I5MmMiXSwiZXhwIjoxNzM2NDQ1MzAxLCJpYXQiOjE3MzY0NDUwMDF9.E0ar-xFbem_Li3gCaqRhu7oFQnQKQevGO5xREVLj3QzKpfteuV4HvPb4z1BxNYDO6ECMyALcI3x9Sl5XUqeE9g"}},"credential_issuer":"http://localhost:8080"}',
    },
    oidc: {
      userinfo: jest.fn().mockImplementation(() => userinfo),
    },
  });
  const { res } = getMockRes();

  it("should render the VCDM credential page", async () => {
    const accessToken = VCDM_TEST_DATA.accessToken;
    const mockTokenResponse = {
      data: {
        access_token: accessToken,
      },
    } as AxiosResponse;
    mockedAxios.post.mockResolvedValueOnce(mockTokenResponse);
    const proofJwt = VCDM_TEST_DATA.proofJwt;
    const mockProofJwtResponse = {
      data: {
        proofJwt: proofJwt,
      },
    } as AxiosResponse;
    mockedAxios.get.mockResolvedValueOnce(mockProofJwtResponse);
    const credential = VCDM_TEST_DATA.credential;
    const mockCredentialResponse = {
      data: {
        credentials: [{ credential: credential }],
      },
    } as AxiosResponse;
    mockedAxios.post.mockResolvedValueOnce(mockCredentialResponse);

    await credentialViewerController(req, res);

    expect(mockedAxios.post).toHaveBeenNthCalledWith(
      1,
      "https://sts-mock.test/token",
      {
        grant_type: "urn:ietf:params:oauth:grant-type:pre-authorized_code",
        "pre-authorized_code":
          "eyJraWQiOiI3OGZhMTMxZDY3N2MxYWMwZjE3MmM1M2I0N2FjMTY5YTk1YWQwZDkyYzM4YmQ3OTRhNzBkYTU5MDMyMDU4Mjc0IiwidHlwIjoiSldUIiwiYWxnIjoiRVMyNTYifQ.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwMDEiLCJjbGllbnRJZCI6IlRFU1RfQ0xJRU5UX0lEIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiY3JlZGVudGlhbF9pZGVudGlmaWVycyI6WyIzNjg0ZWM2ZC05MmRkLTQyYTgtYmY0Yy1mYTIwMGE4M2I5MmMiXSwiZXhwIjoxNzM2NDQ1MzAxLCJpYXQiOjE3MzY0NDUwMDF9.E0ar-xFbem_Li3gCaqRhu7oFQnQKQevGO5xREVLj3QzKpfteuV4HvPb4z1BxNYDO6ECMyALcI3x9Sl5XUqeE9g",
      },
      { headers: { "Content-Type": "application/x-www-form-urlencoded" } },
    );
    expect(mockedAxios.post).toHaveBeenNthCalledWith(
      2,
      "https://example-cri.test/credential",
      {
        proof: {
          jwt: "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRpZDprZXk6ekRuYWVXN2FUQ3R6Sll4TWc1dXJlcXV0V1dNTnFvb25jVGZrdFhNYmI1aVg2OEJweCJ9.eyJpc3MiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJpYXQiOjE3MzY0NDUwMDMzMjYsIm5vbmNlIjoiMmQ4NzJkYzUtMDdlNi00ZmU4LWI1Y2ItYWQ4OWNiYzY4MzcyIn0.D6nzSe_K9oiN3Ux7T2CobYuAiBUruGoXc9tFoSSj0rkwbPWdGCIwQQ-XHqqa803v7hKNOteAnUx6w178SrfDZw",
          proof_type: "jwt",
        },
      },
      {
        headers: {
          Authorization:
            "BEARER eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjJjZWQyMmUyLWMxNWItNGUwMi1hYTVmLTdhMTBhMmVhY2NjNyJ9.eyJzdWIiOiJ1cm46ZmRjOndhbGxldC5hY2NvdW50Lmdvdi51azoyMDI0OkR0UFQ4eC1kcF83M3RubFkzS05UaUNpdHppTjlHRWhlckQxNmJxeE50OWkiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwMDEiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbIjM2ODRlYzZkLTkyZGQtNDJhOC1iZjRjLWZhMjAwYTgzYjkyYyJdLCJjX25vbmNlIjoiMmQ4NzJkYzUtMDdlNi00ZmU4LWI1Y2ItYWQ4OWNiYzY4MzcyIn0.4tIHPhrWzRJhhE8f4OnqRda-y8M10H42r5J5KVPS7iLrFR1amJzCMd3O0KEjVke2ISam9qKe50J9p4qs3O5N-A",
        },
      },
    );
    expect(mockedAxios.get).toHaveBeenCalledWith(
      "https://doc-builder.test/proof-jwt?nonce=2d872dc5-07e6-4fe8-b5cb-ad89cbc68372&audience=http://localhost:8080",
    );
    expect(res.render).toHaveBeenCalledWith("credential.njk", {
      authenticated: true,
      accessToken: accessToken,
      credential: credential,
      credentialClaims:
        '{"sub":"did:key:zDnaeW7aTCtzJYxMg5urequtWWMNqooncTfktXMbb5iX68Bpx","nbf":1736445003,"iss":"http://localhost:8080","context":["https://www.w3.org/2018/credentials/v1"],"exp":1767981003,"iat":1736445003,"vc":{"type":["VerifiableCredential","SocialSecurityCredential"],"credentialSubject":{"name":[{"nameParts":[{"value":"Mr","type":"Title"},{"value":"Sarah","type":"GivenName"},{"value":"Elizabeth","type":"GivenName"},{"value":"Edwards","type":"FamilyName"}]}],"socialSecurityRecord":[{"personalNumber":"QQ123456C"}]}}}',
      preAuthorizedCode:
        "eyJraWQiOiI3OGZhMTMxZDY3N2MxYWMwZjE3MmM1M2I0N2FjMTY5YTk1YWQwZDkyYzM4YmQ3OTRhNzBkYTU5MDMyMDU4Mjc0IiwidHlwIjoiSldUIiwiYWxnIjoiRVMyNTYifQ.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwMDEiLCJjbGllbnRJZCI6IlRFU1RfQ0xJRU5UX0lEIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiY3JlZGVudGlhbF9pZGVudGlmaWVycyI6WyIzNjg0ZWM2ZC05MmRkLTQyYTgtYmY0Yy1mYTIwMGE4M2I5MmMiXSwiZXhwIjoxNzM2NDQ1MzAxLCJpYXQiOjE3MzY0NDUwMDF9.E0ar-xFbem_Li3gCaqRhu7oFQnQKQevGO5xREVLj3QzKpfteuV4HvPb4z1BxNYDO6ECMyALcI3x9Sl5XUqeE9g",
      preAuthorizedCodeClaims: {
        aud: "http://localhost:8001",
        clientId: "TEST_CLIENT_ID",
        iss: "http://localhost:8080",
        credential_identifiers: ["3684ec6d-92dd-42a8-bf4c-fa200a83b92c"],
        exp: 1736445301,
        iat: 1736445001,
      },
      proofJwt:
        "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRpZDprZXk6ekRuYWVXN2FUQ3R6Sll4TWc1dXJlcXV0V1dNTnFvb25jVGZrdFhNYmI1aVg2OEJweCJ9.eyJpc3MiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwODAiLCJpYXQiOjE3MzY0NDUwMDMzMjYsIm5vbmNlIjoiMmQ4NzJkYzUtMDdlNi00ZmU4LWI1Y2ItYWQ4OWNiYzY4MzcyIn0.D6nzSe_K9oiN3Ux7T2CobYuAiBUruGoXc9tFoSSj0rkwbPWdGCIwQQ-XHqqa803v7hKNOteAnUx6w178SrfDZw",
      proofJwtClaims: {
        iss: "urn:fdc:gov:uk:wallet",
        aud: "http://localhost:8080",
        iat: 1736445003326,
        nonce: "2d872dc5-07e6-4fe8-b5cb-ad89cbc68372",
      },
      accessTokenClaims: {
        sub: "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
        iss: "http://localhost:8001",
        aud: "http://localhost:8080",
        credential_identifiers: ["3684ec6d-92dd-42a8-bf4c-fa200a83b92c"],
        c_nonce: "2d872dc5-07e6-4fe8-b5cb-ad89cbc68372",
      },
      credentialClaimsTitle: "VCDM credential",
      credentialSignature: undefined,
      credentialSignaturePayload: undefined,
      x5chain: "",
      x5chainHex: "",
    });
  });

  it("should render the mDoc credential page", async () => {
    const accessToken = MDOC_TEST_DATA.accessToken;
    const mockTokenResponse = {
      data: {
        access_token: accessToken,
      },
    } as AxiosResponse;
    mockedAxios.post.mockResolvedValueOnce(mockTokenResponse);
    const proofJwt = MDOC_TEST_DATA.proofJwt;
    const mockProofJwtResponse = {
      data: {
        proofJwt: proofJwt,
      },
    } as AxiosResponse;
    mockedAxios.get.mockResolvedValueOnce(mockProofJwtResponse);
    const credential = MDOC_TEST_DATA.credential;

    const mockCredentialResponse = {
      data: {
        credentials: [{ credential: credential }],
      },
    } as AxiosResponse;
    mockedAxios.post.mockResolvedValueOnce(mockCredentialResponse);

    await credentialViewerController(req, res);

    expect(mockedAxios.post).toHaveBeenNthCalledWith(
      1,
      "https://sts-mock.test/token",
      {
        grant_type: "urn:ietf:params:oauth:grant-type:pre-authorized_code",
        "pre-authorized_code":
          "eyJraWQiOiI3OGZhMTMxZDY3N2MxYWMwZjE3MmM1M2I0N2FjMTY5YTk1YWQwZDkyYzM4YmQ3OTRhNzBkYTU5MDMyMDU4Mjc0IiwidHlwIjoiSldUIiwiYWxnIjoiRVMyNTYifQ.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwMDEiLCJjbGllbnRJZCI6IlRFU1RfQ0xJRU5UX0lEIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiY3JlZGVudGlhbF9pZGVudGlmaWVycyI6WyIzNjg0ZWM2ZC05MmRkLTQyYTgtYmY0Yy1mYTIwMGE4M2I5MmMiXSwiZXhwIjoxNzM2NDQ1MzAxLCJpYXQiOjE3MzY0NDUwMDF9.E0ar-xFbem_Li3gCaqRhu7oFQnQKQevGO5xREVLj3QzKpfteuV4HvPb4z1BxNYDO6ECMyALcI3x9Sl5XUqeE9g",
      },
      { headers: { "Content-Type": "application/x-www-form-urlencoded" } },
    );
    expect(mockedAxios.post).toHaveBeenNthCalledWith(
      2,
      "https://example-cri.test/credential",
      {
        proof: {
          jwt: "eyJhbGciOiJFUzI1NiIsInR5cCI6Im9wZW5pZDR2Y2ktcHJvb2Yrand0Iiwia2lkIjoiZGlkOmtleTp6RG5hZWVBUnpnc1REaGp3R29BQlRMU3hnUHlBNVQ0blNaNUxWcGFEcUxyek1mWjhyIn0.eyJpc3MiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJhdWQiOiJodHRwczovL3dhbGxldC1jcmVkLWlzc3Vlci1kZHVuZm9yZC1leGFtcGxlLWNyZWRlbnRpYWwtaXNzdWVyLm1vYmlsZS5kZXYuYWNjb3VudC5nb3YudWsiLCJpYXQiOjE3NjY1MDEwNTgyODMsIm5vbmNlIjoiZTJlNjNkYjUtNzE3YS00N2Q2LTgzNTItMzFlNGEyNTI1NjhiIn0.Z5MjJU1W1k0oJvbS8WXNPEl8-YGa9MHE-YSbeCz0u4ryqWUqBCSBbHUCVjmuKVgIwhwgDG6Ga5zcR1eGHv3Pwg",
          proof_type: "jwt",
        },
      },
      {
        headers: {
          Authorization:
            "BEARER eyJhbGciOiJFUzI1NiIsInR5cCI6ImF0K2p3dCIsImtpZCI6IjlhZGQ2NGM5LWJmNmItNGJkMy05YjNhLTU2NzNjNzIxNDMyOSJ9.eyJzdWIiOiJ1cm46ZmRjOndhbGxldC5hY2NvdW50Lmdvdi51azoyMDI0OkR0UFQ4eC1kcF83M3RubFkzS05UaUNpdHppTjlHRWhlckQxNmJxeE50OWkiLCJpc3MiOiJodHRwczovL3dhbGxldC1kb2MtYnVpbGRlci1kZHVuZm9yZC1zdHViLWNyZWRlbnRpYWwtaXNzdWVyLm1vYmlsZS5kZXYuYWNjb3VudC5nb3YudWsiLCJhdWQiOiJodHRwczovL3dhbGxldC1jcmVkLWlzc3Vlci1kZHVuZm9yZC1leGFtcGxlLWNyZWRlbnRpYWwtaXNzdWVyLm1vYmlsZS5kZXYuYWNjb3VudC5nb3YudWsiLCJjcmVkZW50aWFsX2lkZW50aWZpZXJzIjpbIjZlNzcwZTQzLTAzNjAtNGJjZS04NDk1LTUzYWExZTkyYWExZSJdLCJjX25vbmNlIjoiZTJlNjNkYjUtNzE3YS00N2Q2LTgzNTItMzFlNGEyNTI1NjhiIiwiZXhwIjoxNzY2NTAxMjM4LCJqdGkiOiI1MWRiOWEzZi04OTJkLTQyYjgtYWNmNy0wYjc0YTg3N2NhZTgifQ.qKUJvrUp2FjZcaGNsMSg0YD5iYWbxBObHF64kp1ddW-JwGUOp4pSSDUFD5SxL1_EOFXpyxLvKaGi2ACR1FJGhQ",
        },
      },
    );
    expect(mockedAxios.get).toHaveBeenCalledWith(
      "https://doc-builder.test/proof-jwt?nonce=e2e63db5-717a-47d6-8352-31e4a252568b&audience=https://wallet-cred-issuer-ddunford-example-credential-issuer.mobile.dev.account.gov.uk",
    );
    // Verify res.render was called with correct template
    expect(res.render).toHaveBeenCalledWith(
      "credential.njk",
      expect.objectContaining({
        authenticated: true,
        accessToken: accessToken,
        credential: credential,
        credentialClaimsTitle: "mdoc Credential",
        preAuthorizedCode:
          "eyJraWQiOiI3OGZhMTMxZDY3N2MxYWMwZjE3MmM1M2I0N2FjMTY5YTk1YWQwZDkyYzM4YmQ3OTRhNzBkYTU5MDMyMDU4Mjc0IiwidHlwIjoiSldUIiwiYWxnIjoiRVMyNTYifQ.eyJhdWQiOiJodHRwOi8vbG9jYWxob3N0OjgwMDEiLCJjbGllbnRJZCI6IlRFU1RfQ0xJRU5UX0lEIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiY3JlZGVudGlhbF9pZGVudGlmaWVycyI6WyIzNjg0ZWM2ZC05MmRkLTQyYTgtYmY0Yy1mYTIwMGE4M2I5MmMiXSwiZXhwIjoxNzM2NDQ1MzAxLCJpYXQiOjE3MzY0NDUwMDF9.E0ar-xFbem_Li3gCaqRhu7oFQnQKQevGO5xREVLj3QzKpfteuV4HvPb4z1BxNYDO6ECMyALcI3x9Sl5XUqeE9g",
        preAuthorizedCodeClaims: {
          aud: "http://localhost:8001",
          clientId: "TEST_CLIENT_ID",
          iss: "http://localhost:8080",
          credential_identifiers: ["3684ec6d-92dd-42a8-bf4c-fa200a83b92c"],
          exp: 1736445301,
          iat: 1736445001,
        },
        proofJwt: proofJwt,
        proofJwtClaims: {
          iss: "urn:fdc:gov:uk:wallet",
          aud: "https://wallet-cred-issuer-ddunford-example-credential-issuer.mobile.dev.account.gov.uk",
          iat: 1766501058283,
          nonce: "e2e63db5-717a-47d6-8352-31e4a252568b",
        },
        accessTokenClaims: {
          sub: "urn:fdc:wallet.account.gov.uk:2024:DtPT8x-dp_73tnlY3KNTiCitziN9GEherD16bqxNt9i",
          iss: "https://wallet-doc-builder-ddunford-stub-credential-issuer.mobile.dev.account.gov.uk",
          aud: "https://wallet-cred-issuer-ddunford-example-credential-issuer.mobile.dev.account.gov.uk",
          credential_identifiers: ["6e770e43-0360-4bce-8495-53aa1e92aa1e"],
          c_nonce: "e2e63db5-717a-47d6-8352-31e4a252568b",
          exp: 1766501238,
          jti: "51db9a3f-892d-42b8-acf7-0b74a877cae8",
        },
      }),
    );

    // Verify credentialClaims structure by parsing the JSON
    const renderCall = (res.render as jest.Mock).mock.calls.find(
      (call) => call[1]?.credentialClaimsTitle === "mdoc Credential",
    );
    expect(renderCall).toBeDefined();
    const viewData = renderCall[1];
    const credentialClaims = JSON.parse(viewData.credentialClaims);

    // Verify nameSpaces structure
    expect(credentialClaims.nameSpaces).toBeDefined();
    expect(
      credentialClaims.nameSpaces[
        "uk.gov.account.mobile.example-credential-issuer.simplemdoc.1"
      ],
    ).toBeDefined();
    expect(credentialClaims.nameSpaces["org.iso.18013.5.1"]).toBeDefined();

    // Verify specific claims in the simple mdoc namespace
    const simpleMdocClaims =
      credentialClaims.nameSpaces[
        "uk.gov.account.mobile.example-credential-issuer.simplemdoc.1"
      ];
    expect(simpleMdocClaims).toContainEqual(
      expect.objectContaining({
        elementIdentifier: "type_of_fish",
        elementValue: "Coarse fish",
      }),
    );
    expect(simpleMdocClaims).toContainEqual(
      expect.objectContaining({
        elementIdentifier: "number_of_fishing_rods",
        elementValue: 2,
      }),
    );

    // Verify specific claims in the ISO namespace
    const isoClaims = credentialClaims.nameSpaces["org.iso.18013.5.1"];
    expect(isoClaims).toContainEqual(
      expect.objectContaining({
        elementIdentifier: "family_name",
        elementValue: "Fisher",
      }),
    );
    expect(isoClaims).toContainEqual(
      expect.objectContaining({
        elementIdentifier: "given_name",
        elementValue: "John",
      }),
    );

    // Verify date fields contain expected date values (format-agnostic)
    const birthDateClaim = isoClaims.find(
      (c: { elementIdentifier: string }) =>
        c.elementIdentifier === "birth_date",
    );
    expect(birthDateClaim).toBeDefined();
    expect(new Date(birthDateClaim.elementValue).toISOString()).toContain(
      "1980-08-15",
    );

    // Verify x5chain is present and contains certificate data
    expect(viewData.x5chain).toContain("-----BEGIN CERTIFICATE-----");
    expect(viewData.x5chainHex).toBeDefined();
    expect(viewData.x5chainHex.length).toBeGreaterThan(0);
  });

  it("should render an error page when an error happens", async () => {
    mockedAxios.post.mockRejectedValueOnce("SOME_ERROR");

    await credentialViewerController(req, res);

    expect(res.render).toHaveBeenCalledWith("500.njk");
  });
});
