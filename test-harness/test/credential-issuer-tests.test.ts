import {
  getClientId,
  getCredentialFormat,
  getCredentialOfferDeepLink,
  getCriUrl,
  getHasNotificationEndpoint,
  getSelfURL,
  getWalletSubjectId,
} from "../src/config";
import {
  extractCredentialOffer,
  isValidCredentialOffer,
  parseAsJson,
} from "./helpers/credentialOffer/isValidCredentialOffer";
import { isValidMetadata } from "./helpers/metadata/isValidMetadata";
import {
  DidDocument,
  isValidDidWebDocument,
} from "./helpers/didDocument/isValidDidWebDocument";
import { isValidPreAuthorizedCode } from "./helpers/preAuthorizedCode/isValidPreAuthorizedCode";
import { readFileSync } from "fs";
import { decodeJwt, JWK, JWTPayload } from "jose";
import {
  AccessToken,
  createAccessToken,
} from "./helpers/credential/createAccessToken";
import { randomUUID, UUID } from "node:crypto";
import {
  createDidKey,
  createProofJwt,
} from "./helpers/credential/createProofJwt";
import { AxiosError, AxiosResponse } from "axios";
import { isValidJwks } from "./helpers/jwks/isValidJwks";
import {
  getCredential,
  getDidDocument,
  getIacas,
  getJwks,
  getMetadata,
  sendNotification,
} from "./helpers/api/api";
import { Iacas, isValidIacas } from "./helpers/iacas/isValidIacas";
import {
  describeIf,
  itIf,
  isJwt,
  isMdoc,
  hasNotificationEndpoint,
} from "./helpers/testConditions";
import { isValidCredential as isValidJwtCredential } from "./helpers/credential/jwt/isValidCredential";
import { isValidCredential as isValidMdocCredential } from "./helpers/credential/mdoc/isValidCredential";
import {
  wwwAuthenticateHeaderContainsCorrectError,
  wwwAuthenticateHeaderHasNoErrorParams,
} from "./helpers/credential/www-Authenticate";

let CREDENTIAL_OFFER_DEEP_LINK: string;
let CRI_URL: string;
let CRI_DOMAIN: string;
let WALLET_SUBJECT_ID: string;
let PRE_AUTHORIZED_CODE: string;
let PRE_AUTHORIZED_CODE_PAYLOAD: JWTPayload;
let CREDENTIAL_ENDPOINT: string;
let NOTIFICATION_ENDPOINT: string | undefined;
let IACAS_ENDPOINT: string | undefined;
let PRIVATE_KEY_JWK: JWK;
let PUBLIC_KEY_JWK: JWK;
let NONCE: UUID;
let CLIENT_ID: string;
let SELF_URL: string;
let CREDENTIAL_FORMAT: string;
let CREDENTIAL_CONFIGURATION_ID: string;

describe("Credential Issuer Tests", () => {
  beforeAll(async () => {
    CREDENTIAL_OFFER_DEEP_LINK = getCredentialOfferDeepLink();
    const credentialOffer = parseAsJson(
      extractCredentialOffer(CREDENTIAL_OFFER_DEEP_LINK),
    );
    CREDENTIAL_CONFIGURATION_ID =
      credentialOffer.credential_configuration_ids[0];
    PRE_AUTHORIZED_CODE =
      credentialOffer.grants[
        "urn:ietf:params:oauth:grant-type:pre-authorized_code"
      ]["pre-authorized_code"];
    PRE_AUTHORIZED_CODE_PAYLOAD = decodeJwt(PRE_AUTHORIZED_CODE);
    CRI_URL = getCriUrl();
    CRI_DOMAIN = new URL(CRI_URL).hostname;
    WALLET_SUBJECT_ID = getWalletSubjectId();
    const metadata = (await getMetadata(CRI_URL)).data;
    CREDENTIAL_ENDPOINT = metadata.credential_endpoint;
    NOTIFICATION_ENDPOINT = metadata.notification_endpoint;
    IACAS_ENDPOINT = metadata.mdoc_iacas_uri;
    PRIVATE_KEY_JWK = JSON.parse(
      readFileSync("test/helpers/credential/privateKey", "utf8"),
    ) as JWK;
    PUBLIC_KEY_JWK = JSON.parse(
      readFileSync("test/helpers/credential/publicKey", "utf8"),
    ) as JWK;
    NONCE = randomUUID();
    CLIENT_ID = getClientId();
    SELF_URL = getSelfURL();
    CREDENTIAL_FORMAT = getCredentialFormat();
  });

  describe("Credential Offer", () => {
    describe("when validating a provided credential offer", () => {
      it("should be valid credential offer", async () => {
        expect(isValidCredentialOffer(CREDENTIAL_OFFER_DEEP_LINK)).toBe(true);
      });

      it("should be valid pre-authorized code", async () => {
        const jwks = (await getJwks(CRI_URL)).data.keys;

        expect(
          await isValidPreAuthorizedCode(
            PRE_AUTHORIZED_CODE,
            jwks,
            CRI_URL,
            SELF_URL,
            CLIENT_ID,
          ),
        ).toBe(true);
      });
    });
  });

  describe("Metadata", () => {
    let response: AxiosResponse;
    beforeAll(async () => {
      response = await getMetadata(CRI_URL);
    });

    describe("when requesting the credential issuer metadata", () => {
      it("should return 200 status code", () => {
        expect(response.status).toBe(200);
      });

      it("should return JSON content", () => {
        expect(response.headers["content-type"]).toContain("application/json");
        expect(response.data).toBeTruthy();
      });

      it("should return valid metadata", async () => {
        expect(
          await isValidMetadata({
            metadata: response.data,
            criUrl: CRI_URL,
            authServerUrl: SELF_URL,
            credentialFormat: CREDENTIAL_FORMAT,
            credentialConfigurationId: CREDENTIAL_CONFIGURATION_ID,
            hasNotificationEndpoint: getHasNotificationEndpoint() === "true",
          }),
        ).toBe(true);
      });
    });
  });

  describeIf("did:web Document", isJwt, () => {
    let response: AxiosResponse;
    beforeAll(async () => {
      response = await getDidDocument(CRI_URL);
    });

    describe("when requesting the credential issuer did:web document", () => {
      it("should return 200 status code", () => {
        expect(response.status).toBe(200);
      });

      it("should return JSON content", () => {
        expect(response.headers["content-type"]).toContain("application/json");
        expect(response.data).toBeTruthy();
      });

      it("should return valid did:web document", async () => {
        expect(await isValidDidWebDocument(response.data, CRI_DOMAIN)).toBe(
          true,
        );
      });
    });
  });

  describeIf("IACAs", isMdoc, () => {
    describe("when requesting the credential issuer IACAs", () => {
      let response: AxiosResponse;
      beforeAll(async () => {
        response = await getIacas(CRI_URL, IACAS_ENDPOINT);
      });

      it("should return 200 status code", () => {
        expect(response.status).toBe(200);
      });

      it("should return JSON content", () => {
        expect(response.headers["content-type"]).toContain("application/json");
        expect(response.data).toBeTruthy();
      });

      it("should return valid IACAs", async () => {
        expect(await isValidIacas(response.data)).toBe(true);
      });
    });
  });

  describe("JWKS", () => {
    let response: AxiosResponse;
    beforeAll(async () => {
      response = await getJwks(CRI_URL);
    });

    describe("when requesting the credential issuer JWKS", () => {
      it("should return 200 status code", () => {
        expect(response.status).toBe(200);
      });

      it("should return JSON content", () => {
        expect(response.headers["content-type"]).toContain("application/json");
        expect(response.data).toBeTruthy();
      });

      it("should return valid JWKS", async () => {
        expect(await isValidJwks(response.data)).toBe(true);
      });
    });
  });

  describe("Credential (Invalid Requests)", () => {
    describe("when requesting a credential with invalid access token", () => {
      describe("when the access token and credential offer wallet subject IDs do not match", () => {
        it("should return 401 with invalid_token error", async () => {
          const accessTokenWithInvalidWalletSubjectId = (
            await createAccessToken(
              NONCE,
              "not_the_same_wallet_subject_id",
              PRE_AUTHORIZED_CODE_PAYLOAD,
              PRIVATE_KEY_JWK,
            )
          ).access_token;
          const didKey = createDidKey(PUBLIC_KEY_JWK);
          const proofJwt = await createProofJwt(
            NONCE,
            didKey,
            PRE_AUTHORIZED_CODE_PAYLOAD,
            PRIVATE_KEY_JWK,
          );

          try {
            await getCredential(
              accessTokenWithInvalidWalletSubjectId,
              proofJwt,
              CREDENTIAL_ENDPOINT,
            );
          } catch (error) {
            expect((error as AxiosError).response?.status).toEqual(401);
            const header = (error as AxiosError).response?.headers[
              "www-authenticate"
            ];
            expect(wwwAuthenticateHeaderContainsCorrectError(header)).toBe(
              true,
            );
          }
        });
      });

      describe("when the access token signature is invalid", () => {
        it("should return 401 with invalid_token error", async () => {
          const accessToken = (
            await createAccessToken(
              NONCE,
              WALLET_SUBJECT_ID,
              PRE_AUTHORIZED_CODE_PAYLOAD,
              PRIVATE_KEY_JWK,
            )
          ).access_token;
          const accessTokenWithInvalidSignature =
            makeSignatureInvalid(accessToken);
          const didKey = createDidKey(PUBLIC_KEY_JWK);
          const proofJwt = await createProofJwt(
            NONCE,
            didKey,
            PRE_AUTHORIZED_CODE_PAYLOAD,
            PRIVATE_KEY_JWK,
          );

          try {
            await getCredential(
              accessTokenWithInvalidSignature,
              proofJwt,
              CREDENTIAL_ENDPOINT,
            );
          } catch (error) {
            expect((error as AxiosError).response?.status).toEqual(401);
            const header = (error as AxiosError).response?.headers[
              "www-authenticate"
            ];
            expect(wwwAuthenticateHeaderContainsCorrectError(header)).toBe(
              true,
            );
          }
        });
      });
    });

    describe("when requesting a credential with invalid proof JWT", () => {
      describe("when the proof JWT signature is invalid", () => {
        it("should return 400 with invalid_proof error", async () => {
          const proofJwt = await createProofJwt(
            NONCE,
            createDidKey(PUBLIC_KEY_JWK),
            PRE_AUTHORIZED_CODE_PAYLOAD,
            PRIVATE_KEY_JWK,
          );
          const proofJwtWithInvalidSignature = makeSignatureInvalid(proofJwt);
          const accessToken = (
            await createAccessToken(
              NONCE,
              WALLET_SUBJECT_ID,
              PRE_AUTHORIZED_CODE_PAYLOAD,
              PRIVATE_KEY_JWK,
            )
          ).access_token;

          try {
            await getCredential(
              accessToken,
              proofJwtWithInvalidSignature,
              CREDENTIAL_ENDPOINT,
            );
          } catch (error) {
            expect((error as AxiosError).response?.status).toEqual(400);
            expect((error as AxiosError).response?.data).toEqual({
              error: "invalid_proof",
            });
          }
        });
      });

      describe("when the proof JWT nonce does not match the access token c_nonce", () => {
        it("should return 400 with invalid_nonce error", async () => {
          const proofJwtWithMismatchingNonce = await createProofJwt(
            "not_the_same_nonce",
            createDidKey(PUBLIC_KEY_JWK),
            PRE_AUTHORIZED_CODE_PAYLOAD,
            PRIVATE_KEY_JWK,
          );
          const accessToken = (
            await createAccessToken(
              NONCE,
              WALLET_SUBJECT_ID,
              PRE_AUTHORIZED_CODE_PAYLOAD,
              PRIVATE_KEY_JWK,
            )
          ).access_token;

          try {
            await getCredential(
              accessToken,
              proofJwtWithMismatchingNonce,
              CREDENTIAL_ENDPOINT,
            );
          } catch (error) {
            expect((error as AxiosError).response?.status).toEqual(400);
            expect((error as AxiosError).response?.data).toEqual({
              error: "invalid_nonce",
            });
          }
        });
      });
    });
  });

  describe("Credential (Happy Path) & Notification", () => {
    let credentialResponse: AxiosResponse;
    let accessToken: AccessToken;
    let didKey: string;
    beforeAll(async () => {
      accessToken = await createAccessToken(
        NONCE,
        WALLET_SUBJECT_ID,
        PRE_AUTHORIZED_CODE_PAYLOAD,
        PRIVATE_KEY_JWK,
      );
      didKey = createDidKey(PUBLIC_KEY_JWK);
      const PROOF_JWT = await createProofJwt(
        NONCE,
        didKey,
        PRE_AUTHORIZED_CODE_PAYLOAD,
        PRIVATE_KEY_JWK,
      );
      credentialResponse = await getCredential(
        accessToken.access_token,
        PROOF_JWT,
        CREDENTIAL_ENDPOINT,
      );
    });

    describe("Credential", () => {
      describe("when requesting a credential with valid access token and proof JWT", () => {
        it("should return 200 status code", () => {
          expect(credentialResponse.status).toBe(200);
        });

        it("should return JSON content", () => {
          expect(credentialResponse.headers["content-type"]).toContain(
            "application/json",
          );
          expect(credentialResponse.data).toBeTruthy();
        });

        it("should return valid response body", () => {
          expect(credentialResponse.data).toHaveProperty("credentials");
          expect(credentialResponse.data.credentials.length).toEqual(1);
          expect(credentialResponse.data.credentials[0]).toHaveProperty(
            "credential",
          );
        });

        itIf("should return notification_id", hasNotificationEndpoint, () => {
          if (NOTIFICATION_ENDPOINT) {
            expect(credentialResponse.data.notification_id).toBeTruthy();
            expect(credentialResponse.data).toHaveProperty("notification_id");
            expect(typeof credentialResponse.data.notification_id).toBe(
              "string",
            );
          } else {
            expect(credentialResponse.data.notification_id).toBeUndefined();
          }
        });

        itIf("should return valid JWT credential", isJwt, async () => {
          const didDocument: DidDocument = (await getDidDocument(CRI_URL)).data;
          const credential = credentialResponse.data.credentials[0].credential;
          expect(
            await isValidJwtCredential(
              credential,
              didKey,
              didDocument.verificationMethod,
              CRI_URL,
            ),
          ).toBe(true);
        });

        itIf("should return valid mdoc credential", isMdoc, async () => {
          const credential = credentialResponse.data.credentials[0].credential;
          const iacas: Iacas = (await getIacas(CRI_URL, IACAS_ENDPOINT)).data;
          const rootCertificatePem = iacas.data[0].certificatePem;
          expect(
            await isValidMdocCredential(credential, rootCertificatePem),
          ).toBe(true);
        });
      });
    });

    describeIf("Notification", hasNotificationEndpoint, () => {
      describe("when sending valid notifications", () => {
        it("should return 204 for credential_accepted event", async () => {
          const notification_id = credentialResponse.data.notification_id;
          const notificationResponse = await sendNotification(
            accessToken.access_token,
            notification_id,
            "credential_accepted",
            NOTIFICATION_ENDPOINT!,
          );
          expect(notificationResponse.status).toBe(204);
        });

        it("should return 204 for credential_deleted event", async () => {
          const notification_id = credentialResponse.data.notification_id;
          const notificationResponse = await sendNotification(
            accessToken.access_token,
            notification_id,
            "credential_deleted",
            NOTIFICATION_ENDPOINT!,
          );

          expect(notificationResponse.status).toBe(204);
        });

        it("should return 204 for credential_failure event", async () => {
          const notification_id = credentialResponse.data.notification_id;
          const notificationResponse = await sendNotification(
            accessToken.access_token,
            notification_id,
            "credential_failure",
            NOTIFICATION_ENDPOINT!,
          );

          expect(notificationResponse.status).toBe(204);
        });
      });

      describe("when sending invalid notifications", () => {
        it("should return 400 for missing notification_id", async () => {
          try {
            await sendNotification(
              accessToken.access_token,
              undefined,
              "credential_failure",
              NOTIFICATION_ENDPOINT!,
            );
          } catch (error) {
            expect((error as AxiosError).response?.status).toEqual(400);
          }
        });

        it("should return 400 for invalid event type", async () => {
          const notification_id = credentialResponse.data.notification_id;
          try {
            await sendNotification(
              accessToken.access_token,
              notification_id,
              "invalid_event",
              NOTIFICATION_ENDPOINT!,
            );
          } catch (error) {
            expect((error as AxiosError).response?.status).toEqual(400);
          }
        });
      });

      describe("when sending notifications with invalid authentication", () => {
        it("should return 401 for invalid access token", async () => {
          const notification_id = credentialResponse.data.notification_id;
          try {
            await sendNotification(
              "INVALID_TOKEN",
              notification_id,
              "credential_accepted",
              NOTIFICATION_ENDPOINT!,
            );
          } catch (error) {
            expect((error as AxiosError).response?.status).toEqual(401);
            const header = (error as AxiosError).response?.headers[
              "www-authenticate"
            ];
            expect(wwwAuthenticateHeaderContainsCorrectError(header)).toBe(
              true,
            );
          }
        });

        it("should return 401 when no authentication is provided", async () => {
          const notification_id = credentialResponse.data.notification_id;
          try {
            await sendNotification(
              undefined,
              notification_id,
              "credential_accepted",
              NOTIFICATION_ENDPOINT!,
            );
          } catch (error) {
            expect((error as AxiosError).response?.status).toEqual(401);
            const header = (error as AxiosError).response?.headers[
              "www-authenticate"
            ];
            expect(wwwAuthenticateHeaderHasNoErrorParams(header)).toBe(true);
          }
        });
      });
    });
  });

  describe("Credential (Redeeming Offer Twice)", () => {
    describe("when a credential offer twice is redeemed twice", () => {
      it("should return 401 with invalid_token error", async () => {
        const proofJwt = await createProofJwt(
          NONCE,
          createDidKey(PUBLIC_KEY_JWK),
          PRE_AUTHORIZED_CODE_PAYLOAD,
          PRIVATE_KEY_JWK,
        );
        const accessToken = (
          await createAccessToken(
            NONCE,
            WALLET_SUBJECT_ID,
            PRE_AUTHORIZED_CODE_PAYLOAD,
            PRIVATE_KEY_JWK,
          )
        ).access_token;

        try {
          await getCredential(accessToken, proofJwt, CREDENTIAL_ENDPOINT);
        } catch (error) {
          expect((error as AxiosError).response?.status).toEqual(401);
          const header = (error as AxiosError).response?.headers[
            "www-authenticate"
          ];
          expect(wwwAuthenticateHeaderContainsCorrectError(header)).toBe(true);
        }
      });
    });
  });
});

function makeSignatureInvalid(token: string) {
  return token + "makeSignatureInvalid";
}
