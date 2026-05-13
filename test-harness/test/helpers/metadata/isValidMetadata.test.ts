import { isValidMetadata } from "./isValidMetadata";
import { getAjvInstance } from "../ajv/ajvInstance";
import { ErrorObject, ValidateFunction } from "ajv";

const criUrl = "https://cri.example.com";
const authServerUrl = "https://auth.example.com";
const credentialFormat = "jwt";
const credentialConfigurationId = "TestCredential";
const hasNotificationEndpoint = false;

jest.mock("../ajv/ajvInstance", () => ({
  getAjvInstance: jest.fn(),
}));

describe("isValidMetadata", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    (getAjvInstance as jest.Mock).mockReturnValue({
      addSchema: jest.fn().mockReturnThis(),
      compile: jest.fn().mockReturnValue(jest.fn().mockReturnValue(true)),
    });
  });

  it("should throw 'INVALID_METADATA' error when metadata does not comply with schema", async () => {
    const mockValidator = jest
      .fn()
      .mockReturnValue(false) as unknown as ValidateFunction;
    mockValidator.errors = [
      { message: "mock AJV error" } as unknown as ErrorObject,
    ];
    const mockAjv = {
      addSchema: jest.fn().mockReturnThis(),
      compile: jest.fn().mockReturnValue(mockValidator),
    };
    (getAjvInstance as jest.Mock).mockReturnValue(mockAjv);

    const metadata = metadataBuilder().withOverrides({
      credential_configurations_supported: false,
    });

    await expect(
      isValidMetadata({
        metadata,
        criUrl,
        authServerUrl,
        credentialFormat,
        credentialConfigurationId,
        hasNotificationEndpoint,
      }),
    ).rejects.toThrow(
      'INVALID_METADATA: Metadata does not comply with the schema. [{"message":"mock AJV error"}]',
    );
  });

  it("should throw 'INVALID_METADATA' error when 'credential_issuer' is invalid", async () => {
    const metadata = metadataBuilder().withOverrides({
      credential_issuer: "https://something-else.com/",
    });

    await expect(
      isValidMetadata({
        metadata,
        criUrl,
        authServerUrl,
        credentialFormat,
        credentialConfigurationId,
        hasNotificationEndpoint,
      }),
    ).rejects.toThrow(
      'INVALID_METADATA: Invalid "credential_issuer" value. Should be https://cri.example.com but found https://something-else.com/',
    );
  });

  it("should throw 'INVALID_METADATA' error when 'authorization_servers' is invalid", async () => {
    const metadata = metadataBuilder().withOverrides({
      authorization_servers: ["https://something-else.com/"],
    });

    await expect(
      isValidMetadata({
        metadata,
        criUrl,
        authServerUrl,
        credentialFormat,
        credentialConfigurationId,
        hasNotificationEndpoint,
      }),
    ).rejects.toThrow(
      'INVALID_METADATA: Invalid "authorization_servers" value. Should contain https://auth.example.com but only contains https://something-else.com/',
    );
  });

  it("should throw 'INVALID_METADATA' error when 'credential_endpoint' is invalid", async () => {
    const metadata = metadataBuilder().withOverrides({
      credential_endpoint: "https://something-else.com/something",
    });

    await expect(
      isValidMetadata({
        metadata,
        criUrl,
        authServerUrl,
        credentialFormat,
        credentialConfigurationId,
        hasNotificationEndpoint,
      }),
    ).rejects.toThrow(
      'INVALID_METADATA: Invalid "credential_endpoint" value. Should be https://cri.example.com/credential but found https://something-else.com/something',
    );
  });

  it("should throw 'INVALID_METADATA' error when credential is not in 'credential_configurations_supported'", async () => {
    const metadata = metadataBuilder().withOverrides({
      credential_configurations_supported: {
        UnknownCredential: {},
      },
    });

    await expect(
      isValidMetadata({
        metadata,
        criUrl,
        authServerUrl,
        credentialFormat,
        credentialConfigurationId,
        hasNotificationEndpoint,
      }),
    ).rejects.toThrow(
      'INVALID_METADATA: Invalid "credential_configurations_supported" value. Missing credential TestCredential',
    );
  });

  describe("given the credential issuer implements the notification endpoint", () => {
    it("should throw 'INVALID_METADATA' error when 'notification_endpoint' is mising", async () => {
      const metadata = metadataBuilder().withOverrides({
        notification_endpoint: undefined,
      });

      await expect(
        isValidMetadata({
          metadata,
          criUrl,
          authServerUrl,
          credentialFormat,
          credentialConfigurationId,
          hasNotificationEndpoint: true,
        }),
      ).rejects.toThrow(
        "INVALID_METADATA: Invalid metadata. Missing notification_endpoint",
      );
    });

    it("should throw 'INVALID_METADATA' error when 'notification_endpoint' is invalid", async () => {
      const metadata = metadataBuilder().withOverrides({
        notification_endpoint: "https://something-else.com/something",
      });

      await expect(
        isValidMetadata({
          metadata,
          criUrl,
          authServerUrl,
          credentialFormat,
          credentialConfigurationId,
          hasNotificationEndpoint: true,
        }),
      ).rejects.toThrow(
        'INVALID_METADATA: Invalid "notification_endpoint" value. Should be https://cri.example.com/notification but found https://something-else.com/something',
      );
    });

    it("should return true when 'notification_endpoint' is valid", async () => {
      const metadata = metadataBuilder().withOverrides({
        notification_endpoint: criUrl + "/notification",
      });

      expect(
        await isValidMetadata({
          metadata,
          criUrl,
          authServerUrl,
          credentialFormat,
          credentialConfigurationId,
          hasNotificationEndpoint: true,
        }),
      ).toEqual(true);
    });
  });

  describe("given the credential format is mDoc ('mdoc')", () => {
    it("should throw 'INVALID_METADATA' error when 'mdoc_iacas_uri' is missing", async () => {
      const metadata = metadataBuilder().withDefaults();

      await expect(
        isValidMetadata({
          metadata,
          criUrl,
          authServerUrl,
          credentialFormat: "mdoc",
          credentialConfigurationId,
          hasNotificationEndpoint,
        }),
      ).rejects.toThrow(
        "INVALID_METADATA: Invalid metadata. Missing mdoc_iacas_uri",
      );
    });

    it("should throw 'INVALID_METADATA' error when 'mdoc_iacas_uri' is invalid", async () => {
      const metadata = metadataBuilder().withOverrides({
        mdoc_iacas_uri: "https://something-else.com/something",
      });

      await expect(
        isValidMetadata({
          metadata,
          criUrl,
          authServerUrl,
          credentialFormat: "mdoc",
          credentialConfigurationId,
          hasNotificationEndpoint,
        }),
      ).rejects.toThrow(
        'INVALID_METADATA: Invalid "mdoc_iacas_uri" value. Should be https://cri.example.com/iacas but found https://something-else.com/something',
      );
    });

    it("should return true when metadata is valid", async () => {
      const metadata = metadataBuilder().withOverrides({
        mdoc_iacas_uri: criUrl + "/iacas",
      });

      expect(
        await isValidMetadata({
          metadata,
          criUrl,
          authServerUrl,
          credentialFormat: "mdoc",
          credentialConfigurationId,
          hasNotificationEndpoint,
        }),
      ).toEqual(true);
    });
  });

  describe("given the credential format is JWT ('jwt')", () => {
    it("should return true when metadata is valid", async () => {
      const metadata = metadataBuilder().withDefaults();

      expect(
        await isValidMetadata({
          metadata,
          criUrl,
          authServerUrl,
          credentialFormat: "jwt",
          credentialConfigurationId,
          hasNotificationEndpoint,
        }),
      ).toEqual(true);
    });
  });
});

function metadataBuilder<T>(): {
  withDefaults();
  withOverrides(overrides: T);
} {
  const defaults = {
    credential_issuer: criUrl,
    authorization_servers: [authServerUrl],
    credential_endpoint: criUrl + "/credential",
    credential_configurations_supported: { TestCredential: {} },
  };
  return {
    withDefaults() {
      return { ...defaults };
    },
    withOverrides(overrides: T) {
      return { ...defaults, ...overrides };
    },
  };
}
