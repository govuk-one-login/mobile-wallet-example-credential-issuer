import { isValidCredential } from "./isValidCredential";
import { TestMDLBuilder } from "./TestMDLBuilder";
import { MDLValidationError } from "./MDLValidationError";
import { Tag } from "cbor2";
import { base64url } from "jose";
import * as ajvModule from "../../ajv/ajvInstance";
import { X509Certificate } from "node:crypto";
import { ErrorObject, ValidateFunction } from "ajv";

const rootCertificate = `-----BEGIN CERTIFICATE-----
MIIB1zCCAX2gAwIBAgIUIatAsTQsYXy6Wrb1Cdp8tJ3RLC0wCgYIKoZIzj0EAwIw
QTELMAkGA1UEBhMCR0IxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9vdCAt
IExPQ0FMIGVudmlyb25tZW50MB4XDTI1MDkwMjEwMjQyNVoXDTI4MDYyMjEwMjQy
NVowQTELMAkGA1UEBhMCR0IxMjAwBgNVBAMMKW1ETCBFeGFtcGxlIElBQ0EgUm9v
dCAtIExPQ0FMIGVudmlyb25tZW50MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE
mBxJk2MqFKn7c4MSEwlA8EUbMMxyU8DnPXwERUs4VjBF7534WDQQLCZBxvaYn73M
35NYkWiXO8oiRmWG9AzDn6NTMFEwHQYDVR0OBBYEFPY4eri7CuGrxh14YMTQe1qn
BVjoMB8GA1UdIwQYMBaAFPY4eri7CuGrxh14YMTQe1qnBVjoMA8GA1UdEwEB/wQF
MAMBAf8wCgYIKoZIzj0EAwIDSAAwRQIgPJmIjY1hoYRHjBMgLeV0x+wWietEyBfx
zyaulhhqnewCIQCmJ0kwBidqVzCOIx5H8CaEHUnTA/ULJGC2DDFzT7s54A==
-----END CERTIFICATE-----`;

describe("isValidCredential", () => {
  beforeEach(() => {
    jest.useFakeTimers().setSystemTime(new Date("2025-09-10T15:30:00Z"));
  });

  afterEach(() => {
    jest.useRealTimers();
    ajvModule.resetAjvInstance();
    jest.restoreAllMocks();
  });

  describe("Encoding", () => {
    it("should throw MDLValidationError for invalid base64url encoding", async () => {
      expect.assertions(2);
      try {
        await isValidCredential("invalid@base64url!", rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to decode base64url encoded credential - The input to be decoded is not correctly encoded.",
        );
      }
    });

    it("should throw MDLValidationError for invalid CBOR encoding", async () => {
      expect.assertions(2);
      try {
        await isValidCredential(
          base64url.encode("invalidCbor"),
          rootCertificate,
        );
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to decode CBOR encoded credential - Extra data in input",
        );
      }
    });
  });

  describe("Tags", () => {
    it("should throw MDLValidationError when an IssuerSignedItem in namespace org.iso.18013.5.1 is not tagged with 24", async () => {
      const credential = new TestMDLBuilder()
        .withUntaggedIssuerSignedItemBytes("family_name")
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(Error);
        expect((error as Error).message).toContain(
          "IssuerSignedItem in namespace 'org.iso.18013.5.1' missing tag '24'",
        );
      }
    });

    it("should throw MDLValidationError when an IssuerSignedItem in namespace org.iso.18013.5.1.GB is not tagged with 24", async () => {
      const credential = new TestMDLBuilder()
        .withUntaggedIssuerSignedItemBytes("welsh_licence")
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to validate tags - IssuerSignedItem in namespace 'org.iso.18013.5.1.GB' missing tag '24'",
        );
      }
    });

    it("should throw MDLValidationError when 'expiry_date' is not tagged with 1004", async () => {
      const credential = new TestMDLBuilder()
        .withElementValue("expiry_date", "2030-01-01")
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to validate tags - 'expiry_date' missing tag '1004'",
        );
      }
    });

    it("should throw MDLValidationError when 'issue_date' in driving privileges is not tagged with 1004", async () => {
      const credential = new TestMDLBuilder()
        .withElementValue("driving_privileges", [
          {
            vehicle_category_code: "C1",
            issue_date: "2029-05-10",
          },
        ])
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to validate tags - 'issue_date' in 'driving_privileges' missing tag '1004'",
        );
      }
    });

    it("should throw MDLValidationError when 'issue_date' in provisional driving privileges is not tagged with 1004", async () => {
      const credential = new TestMDLBuilder()
        .withElementValue("provisional_driving_privileges", [
          {
            vehicle_category_code: "C1",
            issue_date: "2029-05-10",
          },
        ])
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to validate tags - 'issue_date' in 'provisional_driving_privileges' missing tag '1004'",
        );
      }
    });

    it("should throw MDLValidationError when 'expiry_date' in driving privileges is not tagged with 1004", async () => {
      const credential = new TestMDLBuilder()
        .withElementValue("driving_privileges", [
          {
            vehicle_category_code: "C1",
            expiry_date: "2029-05-10",
          },
        ])
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to validate tags - 'expiry_date' in 'driving_privileges' missing tag '1004'",
        );
      }
    });

    it("should throw MDLValidationError when 'expiry_date' in provisional driving privileges is not tagged with 1004", async () => {
      const credential = new TestMDLBuilder()
        .withElementValue("provisional_driving_privileges", [
          {
            vehicle_category_code: "C1",
            expiry_date: "2029-05-10",
          },
        ])
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to validate tags - 'expiry_date' in 'provisional_driving_privileges' missing tag '1004'",
        );
      }
    });

    it("should throw MDLValidationError when MobileSecurityObjectBytes missing tag '24'", async () => {
      const credential = new TestMDLBuilder().withUntaggedMsoBytes().build();

      await expect(
        isValidCredential(credential, rootCertificate),
      ).rejects.toThrow("MobileSecurityObjectBytes missing tag");
    });

    it("should throw MDLValidationError when 'signed' in ValidityInfo is not tagged with 0", async () => {
      const credential = new TestMDLBuilder()
        .withValidityInfo({
          signed: "2025-12-20T15:20:33Z",
        })
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to validate tags - 'signed' in 'ValidityInfo' missing tag 0",
        );
      }
    });

    it("should throw MDLValidationError when 'validFrom' in ValidityInfo is not tagged with 0", async () => {
      const credential = new TestMDLBuilder()
        .withValidityInfo({
          validFrom: "2025-12-20T15:20:33",
        })
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to validate tags - 'validFrom' in 'ValidityInfo' missing tag 0",
        );
      }
    });

    it("should throw MDLValidationError when 'validUntil' in ValidityInfo is not tagged with 0", async () => {
      const credential = new TestMDLBuilder()
        .withValidityInfo({
          validUntil: "2025-12-20T15:20:33",
        })
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Failed to validate tags - 'validUntil' in 'ValidityInfo' missing tag 0",
        );
      }
    });
  });

  describe("IssuerSigned Schema", () => {
    it("should throw MDLValidationError with AJV error", async () => {
      const mockValidator = jest
        .fn()
        .mockReturnValue(false) as unknown as ValidateFunction;
      mockValidator.errors = [
        {
          instancePath: "/path",
          message: "must be a string",
          data: 123,
          keyword: "key",
        } as unknown as ErrorObject,
      ];

      const mockAjv = {
        addSchema: jest.fn().mockReturnThis(),
        compile: jest.fn().mockReturnValue(mockValidator),
      };

      jest.spyOn(ajvModule, "getAjvInstance").mockReturnValue(mockAjv as never);

      const credential = new TestMDLBuilder().build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "IssuerSigned does not comply with schema - /path: must be a string",
        );
      }
    });

    it("should throw MDLValidationError and default path to 'root' when instancePath is missing", async () => {
      const mockValidator = jest
        .fn()
        .mockReturnValue(false) as unknown as ValidateFunction;
      mockValidator.errors = [
        {
          instancePath: "",
          message: "must be a string",
          data: 123,
          keyword: "key",
        } as unknown as ErrorObject,
      ];

      const mockAjv = {
        addSchema: jest.fn().mockReturnThis(),
        compile: jest.fn().mockReturnValue(mockValidator),
      };

      jest.spyOn(ajvModule, "getAjvInstance").mockReturnValue(mockAjv as never);

      const credential = new TestMDLBuilder().build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "IssuerSigned does not comply with schema - root: must be a string",
        );
      }
    });

    it("should throw MDLValidationError and default message to 'Unknown validation error' when message is missing", async () => {
      const mockValidator = jest
        .fn()
        .mockReturnValue(false) as unknown as ValidateFunction;
      mockValidator.errors = [
        {
          instancePath: "/path",
          message: undefined,
          data: 123,
          keyword: "key",
        } as unknown as ErrorObject,
      ];

      const mockAjv = {
        addSchema: jest.fn().mockReturnThis(),
        compile: jest.fn().mockReturnValue(mockValidator),
      };

      jest.spyOn(ajvModule, "getAjvInstance").mockReturnValue(mockAjv as never);

      const credential = new TestMDLBuilder().build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "IssuerSigned does not comply with schema - /path: Unknown validation error",
        );
      }
    });

    it("should throw MDLValidationError with empty error details when validator.errors is undefined", async () => {
      const mockValidator = jest
        .fn()
        .mockReturnValue(false) as unknown as ValidateFunction;
      mockValidator.errors = undefined;

      const mockAjv = {
        addSchema: jest.fn().mockReturnThis(),
        compile: jest.fn().mockReturnValue(mockValidator),
      };

      jest.spyOn(ajvModule, "getAjvInstance").mockReturnValue(mockAjv as never);

      const credential = new TestMDLBuilder().build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "IssuerSigned does not comply with schema - ",
        );
      }
    });
  });

  describe("Digest IDs", () => {
    it("should throw MDLValidationError when digest IDs within the org.iso.18013.5.1 namespace are not unique", async () => {
      const credential = new TestMDLBuilder()
        .withDigestId("given_name", 10)
        .withDigestId("family_name", 10)
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Digest IDs are not unique for namespace org.iso.18013.5.1",
        );
      }
    });

    it("should throw MDLValidationError when digest IDs within the org.iso.18013.5.1.GB namespace are not unique", async () => {
      const credential = new TestMDLBuilder()
        .withDigestId("welsh_licence", 10)
        .withDigestId("title", 10)
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Digest IDs are not unique for namespace org.iso.18013.5.1.GB",
        );
      }
    });
  });

  describe("Portrait", () => {
    it("should throw MDLValidationError when first byte is invalid", async () => {
      const credential = new TestMDLBuilder()
        .withElementValue("portrait", new Uint8Array([0xff, 0xd8, 0xff, 0xe1]))
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Invalid SOI - JPEG should start with ffd8ffe0 or ffd8ffee or ffd8ffdb but found ffd8ffe1",
        );
      }
    });

    it("should throw MDLValidationError when penultimate byte is invalid", async () => {
      const credential = new TestMDLBuilder()
        .withElementValue(
          "portrait",
          new Uint8Array([0xff, 0xd8, 0xff, 0xe0, 0x00, 0xd9]),
        )
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Invalid EOI - JPEG should end with ffd9 but found 00d9",
        );
      }
    });

    it("should throw MDLValidationError when last byte is invalid", async () => {
      const credential = new TestMDLBuilder()
        .withElementValue("portrait", new Uint8Array([0xff, 0xd8, 0xff, 0xe0]))
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toBe(
          "Invalid EOI - JPEG should end with ffd9 but found ffe0",
        );
      }
    });

    it("should throw MDLValidationError when portrait is empty", async () => {
      const credential = new TestMDLBuilder()
        .withElementValue("portrait", new Uint8Array([]))
        .build();

      expect.assertions(2);
      try {
        await isValidCredential(credential, rootCertificate);
      } catch (error) {
        expect(error).toBeInstanceOf(MDLValidationError);
        expect((error as Error).message).toContain("Invalid SOI");
      }
    });
  });

  describe("IssuerAuth", () => {
    describe("Protected header", () => {
      it("should throw MDLValidationError when protected header is not a Map", async () => {
        const credential = new TestMDLBuilder()
          .withProtectedHeader("not a map")
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "Protected header is not a Map",
          );
        }
      });

      it("should throw MDLValidationError when protected header has more than one key", async () => {
        const credential = new TestMDLBuilder()
          .withProtectedHeader(new Map().set(1, -7).set(2, "b"))
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "Protected header contains unexpected extra parameters - must contain only one",
          );
        }
      });

      it("should throw MDLValidationError when protected header is missing algorithm (1) key", async () => {
        const credential = new TestMDLBuilder()
          .withProtectedHeader(new Map().set(2, -7))
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            'Protected header missing "alg" (1)',
          );
        }
      });

      it("should throw MDLValidationError when protected header algorithm is not ES256 (-1)", async () => {
        const credential = new TestMDLBuilder()
          .withProtectedHeader(new Map().set(1, 7))
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            'Protected header "alg" must be -7 (ES256)',
          );
        }
      });
    });

    describe("Unprotected header", () => {
      it("should throw MDLValidationError when unprotected header has more than one key", async () => {
        const credential = new TestMDLBuilder()
          .withUnprotectedHeader(
            new Map().set(33, new Uint8Array()).set(2, new Uint8Array()),
          )
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "Unprotected header contains unexpected extra parameters - must contain only one",
          );
        }
      });

      it("should throw MDLValidationError when unprotected header is missing x5chain (33) key", async () => {
        const credential = new TestMDLBuilder()
          .withUnprotectedHeader(new Map().set(1, new Uint8Array()))
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            'Unprotected header missing "x5chain" (33)',
          );
        }
      });

      it("should throw MDLValidationError when certificate is not a valid X509 certificate", async () => {
        const credential = new TestMDLBuilder()
          .withUnprotectedHeader(new Map().set(33, new Uint8Array()))
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toEqual(
            expect.stringContaining(
              "Failed to parse document signing certificate as X509Certificate",
            ),
          );
        }
      });

      it("should throw MDLValidationError when document signing certificate is a CA", async () => {
        const caCertificate = new X509Certificate(rootCertificate);
        const credential = new TestMDLBuilder()
          .withUnprotectedHeader(
            new Map().set(33, new Uint8Array(caCertificate.raw)),
          )
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "Document signing certificate must not be a CA certificate",
          );
        }
      });

      it("should throw MDLValidationError when document signing certificate is not valid at the current time", async () => {
        jest.useFakeTimers();
        jest.setSystemTime(new Date("2025-09-10T13:38:48Z"));
        // Current time: 2025-09-10, Certificate valid from: 2026-01-23
        const notYetValidCertificatePem = `-----BEGIN CERTIFICATE-----
MIIBaTCCAQ+gAwIBAgIURf+h7qmhNPgAaEaPTcVxS9VHCs8wCgYIKoZIzj0EAwIw
DTELMAkGA1UEBhMCR0IwHhcNMjYwMTIzMTkyMzMzWhcNMjcwMTIzMTkyMzMzWjAN
MQswCQYDVQQGEwJHQjBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABGeeOjZn8fE8
VlYczaP1WxltIBRFS7GYDd2tCwjnWQTb8bkcduDJgkUY5F7oSPIXXt62DxB6e5eN
8EvSn3nmQR6jTTBLMAkGA1UdEwQCMAAwHQYDVR0OBBYEFB44hqfYqP0ffiWBMv3/
FvTJh8vMMB8GA1UdIwQYMBaAFJXgNJAHxslWE68pACiQGvlY335IMAoGCCqGSM49
BAMCA0gAMEUCIQC2c028yzpQCh2Azw/YHpxOzn+ZxKvqpHrk8ysE7KY9ygIgZD51
P1oagJM6zj+3hIFOq8se0YLBI8S9sWUVsxluiN4=
-----END CERTIFICATE-----`;

        const notYetValidCertificate = new X509Certificate(
          notYetValidCertificatePem,
        );
        const credential = new TestMDLBuilder()
          .withUnprotectedHeader(
            new Map().set(33, new Uint8Array(notYetValidCertificate.raw)),
          )
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "Document signing certificate is not valid at the current time",
          );
        }
      });

      it("should throw MDLValidationError when certificate issuer does not match root subject", async () => {
        jest.useFakeTimers();
        jest.setSystemTime(new Date("2026-06-01T13:38:48Z"));
        const mismatchedIssuerCertificatePem = `-----BEGIN CERTIFICATE-----
MIIBajCCAQ+gAwIBAgIUaISZZlk1t+jLC9SyUnYcl4c7gTkwCgYIKoZIzj0EAwIw
DTELMAkGA1UEBhMCR0IwHhcNMjYwMTIzMTk1MjQ4WhcNMjcwMTIzMTk1MjQ4WjAN
MQswCQYDVQQGEwJHQjBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABAHGaZhNOqIx
otdbjr4RXPk7sieLzT3dX7laB/b2TDPCEW+xbRGq0tItxubAz1k8c/ZBjpY3v6NK
uK57UPOxKomjTTBLMAkGA1UdEwQCMAAwHQYDVR0OBBYEFOende87vaMlo0+3LABk
YKLizuksMB8GA1UdIwQYMBaAFOEhaMIg0DOmzJcXORzaUU+fmZJ3MAoGCCqGSM49
BAMCA0kAMEYCIQD8eg+NH2fDlojqX6YQ5faB9nuXE3yAbbuL6V45sF2MywIhALuL
1SCmoCBIHknFWIY6MdUiT9JqBVYud5RarNd2ELU9
-----END CERTIFICATE-----`;

        const mismatchedIssuerCertificate = new X509Certificate(
          mismatchedIssuerCertificatePem,
        );

        const credential = new TestMDLBuilder()
          .withUnprotectedHeader(
            new Map().set(33, new Uint8Array(mismatchedIssuerCertificate.raw)),
          )
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "Certificate issuer does not match root subject",
          );
        }
      });

      it("should throw MDLValidationError when document signing certificate signature fails to verify", async () => {
        jest.useFakeTimers();
        jest.setSystemTime(new Date("2026-06-01T13:38:48Z"));

        const wrongRootCertificatePem = `-----BEGIN CERTIFICATE-----
MIIBaTCCAQ+gAwIBAgIURf+h7qmhNPgAaEaPTcVxS9VHCs8wCgYIKoZIzj0EAwIw
DTELMAkGA1UEBhMCR0IwHhcNMjYwMTIzMTkyMzMzWhcNMjcwMTIzMTkyMzMzWjAN
MQswCQYDVQQGEwJHQjBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABGeeOjZn8fE8
VlYczaP1WxltIBRFS7GYDd2tCwjnWQTb8bkcduDJgkUY5F7oSPIXXt62DxB6e5eN
8EvSn3nmQR6jTTBLMAkGA1UdEwQCMAAwHQYDVR0OBBYEFB44hqfYqP0ffiWBMv3/
FvTJh8vMMB8GA1UdIwQYMBaAFJXgNJAHxslWE68pACiQGvlY335IMAoGCCqGSM49
BAMCA0gAMEUCIQC2c028yzpQCh2Azw/YHpxOzn+ZxKvqpHrk8ysE7KY9ygIgZD51
P1oagJM6zj+3hIFOq8se0YLBI8S9sWUVsxluiN4=
-----END CERTIFICATE-----`; // Valid From	Fri, 23 Jan 2026 19:52:48 UTC // Valid To	Sat, 23 Jan 2027 19:52:48 UTC
        const root = new X509Certificate(wrongRootCertificatePem);
        const corruptedCert = new Uint8Array(root.raw);
        corruptedCert[corruptedCert.length - 10] ^= 0xff;

        const credential = new TestMDLBuilder()
          .withUnprotectedHeader(new Map().set(33, corruptedCert))
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, wrongRootCertificatePem);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "Document signing certificate signature not verified",
          );
        }
      });

      it("should throw MDLValidationError when certificate.verify throws an error", async () => {
        jest.useFakeTimers();
        jest.setSystemTime(new Date("2026-06-01T13:38:48Z"));
        const root = `-----BEGIN CERTIFICATE-----
MIIBcDCCARWgAwIBAgIUBEchMrG4TkaH1GCFT9g4aavAl/0wCgYIKoZIzj0EAwIw
DTELMAkGA1UEBhMCR0IwHhcNMjYwMjA2MTg1NDM1WhcNMjgxMTI2MTg1NDM1WjAN
MQswCQYDVQQGEwJHQjBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABL3Tlt3/IOay
YdXEon4ewumUzsXI9YzPKeZ2BotkqN6v+rq5YbiwpR1gvw7O4I9935T6bsAhQizJ
P6bzH6sxUQOjUzBRMB0GA1UdDgQWBBTdsH0VK3ME3dXqAVUbAjWUGsd4WTAfBgNV
HSMEGDAWgBTdsH0VK3ME3dXqAVUbAjWUGsd4WTAPBgNVHRMBAf8EBTADAQH/MAoG
CCqGSM49BAMCA0kAMEYCIQCswZ7AEN7C1BXUozJzfpSutZZ/dFCvqeL4t6h9Da15
mgIhANG2+hz/ejZdUjVcjtDdN+/18Wus8gs9vdveWu9SeoJ7
-----END CERTIFICATE-----`;
        const serverCertPem = `-----BEGIN CERTIFICATE-----
MIIBXzCCAQSgAwIBAgIUG3qUgKL8F+aOHk32XWfbkqQTscgwCgYIKoZIzj0EAwIw
DTELMAkGA1UEBhMCR0IwHhcNMjYwMjA2MTg1NTExWhcNMjcwMjA2MTg1NTExWjAN
MQswCQYDVQQGEwJHQjBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABOXFsTMRj4fU
tvM1QYsmsus81yuCkfV4lDcZEg6u8FydSg5FdXFYxzyU39MczeXXNUJqAN2XnuHf
JlzEAsRP2QujQjBAMB0GA1UdDgQWBBQQLWmAtbNhbHhI82ZqZrqD78QmjTAfBgNV
HSMEGDAWgBTdsH0VK3ME3dXqAVUbAjWUGsd4WTAKBggqhkjOPQQDAgNJADBGAiEA
pUMhLrs/OBOz/HPHLhtA6WW2TqqG9xLVMGwFrEQDRjUCIQCkl26uhVBpZ7xaUyZD
oqkvy0k40yR/ej0XvNwSLKHIyQ==
-----END CERTIFICATE-----`;
        const certificate = new X509Certificate(serverCertPem);
        const credential = new TestMDLBuilder()
          .withUnprotectedHeader(
            new Map().set(33, new Uint8Array(certificate.raw)),
          )
          .build();
        const verifySpy = jest
          .spyOn(X509Certificate.prototype, "verify")
          .mockImplementation(() => {
            throw new Error("crypto failure");
          });

        expect.assertions(2);
        try {
          await isValidCredential(credential, root);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "Signature could not be verified - crypto failure",
          );
        } finally {
          verifySpy.mockRestore();
        }
      });

      it("should throw MDLValidationError when MSO signature fails to verify", async () => {
        jest.useFakeTimers();
        jest.setSystemTime(new Date("2026-01-10T13:38:48Z"));
        const rootCertificate = `-----BEGIN CERTIFICATE-----
MIICDTCCAbOgAwIBAgIULjpCx753jPKhnnOzt6AqxMuH/MkwCgYIKoZIzj0EAwIw
XDELMAkGA1UEBhMCVUsxDzANBgNVBAgMBkxvbmRvbjEPMA0GA1UEBwwGTG9uZG9u
MQ0wCwYDVQQKDARUZXN0MQ0wCwYDVQQLDARUZXN0MQ0wCwYDVQQDDARUZXN0MB4X
DTI2MDEwODEzMzg0OFoXDTI4MTAyODEzMzg0OFowXDELMAkGA1UEBhMCVUsxDzAN
BgNVBAgMBkxvbmRvbjEPMA0GA1UEBwwGTG9uZG9uMQ0wCwYDVQQKDARUZXN0MQ0w
CwYDVQQLDARUZXN0MQ0wCwYDVQQDDARUZXN0MFkwEwYHKoZIzj0CAQYIKoZIzj0D
AQcDQgAEWWHkpEFWKYuqfDe8zVW0AVMFn0o0p6cW6K7kEbRjLJmqFfG+RcfBUJdr
nrOwa2pL5QDDoxzrWr8G84179bKBaqNTMFEwHQYDVR0OBBYEFOuameupM0YpmgBT
5Q4WxFe6TVMUMB8GA1UdIwQYMBaAFOuameupM0YpmgBT5Q4WxFe6TVMUMA8GA1Ud
EwEB/wQFMAMBAf8wCgYIKoZIzj0EAwIDSAAwRQIgVvfBUEP5PZCKX173c0y7kyZm
t1jfnQrTHh3z2ale/FUCIQCePJrmnE1+WFyYJylg+RYLBx2OmpA7+gOzQyVFTDKh
Bg==
-----END CERTIFICATE-----`; //Valid From	Thu, 08 Jan 2026 13:38:48 UTC // Valid To	Sat, 28 Oct 2028 13:38:48 UTC

        const wrongDocumentSigningCertificate =
          new X509Certificate(`-----BEGIN CERTIFICATE-----
MIIB7TCCAZOgAwIBAgIUZpfeB6WGkUsUk13SiJX8i6vG1IEwCgYIKoZIzj0EAwIw
XDELMAkGA1UEBhMCVUsxDzANBgNVBAgMBkxvbmRvbjEPMA0GA1UEBwwGTG9uZG9u
MQ0wCwYDVQQKDARUZXN0MQ0wCwYDVQQLDARUZXN0MQ0wCwYDVQQDDARUZXN0MB4X
DTI2MDEwODEzMzkzNVoXDTI3MDEwODEzMzkzNVowTTELMAkGA1UEBhMCVUsxDzAN
BgNVBAgMBkxvbmRvbjEPMA0GA1UEBwwGTG9uZG9uMQ0wCwYDVQQKDARUZXN0MQ0w
CwYDVQQLDARUZXN0MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+jfaNAHbEm+P
2QbR6EMOj7+nILxkSJIani1RIPJI2X/NTtwJbMq6TN7X7f9BtK5DsioNOThMF/+t
1EFaLFPAuKNCMEAwHQYDVR0OBBYEFL0/RS4sYeY0F/AvLmHbEEv9NSG4MB8GA1Ud
IwQYMBaAFOuameupM0YpmgBT5Q4WxFe6TVMUMAoGCCqGSM49BAMCA0gAMEUCIEBO
RlvvhrfRUeNSJ0B18SsHCw1r4YUoJ206JZPFWxsRAiEA39zuNQ4ituFpufYFAUzb
h6XK6xERRLkY5jjINTt8TkU=
-----END CERTIFICATE-----`);
        const credential = new TestMDLBuilder()
          .withUnprotectedHeader(
            new Map().set(
              33,
              new Uint8Array(wrongDocumentSigningCertificate.raw),
            ),
          )
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe("Signature not verified");
        } finally {
          jest.useRealTimers();
        }
      });
    });

    describe("MSO Schema", () => {
      it("should should throw MDLValidationError for MSO with AJV error", async () => {
        const mockValidator = jest
          .fn()
          .mockReturnValueOnce(true) // first call IssuerSigned (valid)
          .mockReturnValueOnce(false) as unknown as ValidateFunction; // second call MSO (invalid)
        mockValidator.errors = [
          {
            instancePath: "/path",
            message: "must be a string",
            data: 123,
            keyword: "key",
          } as unknown as ErrorObject,
        ];

        const mockAjv = {
          addSchema: jest.fn().mockReturnThis(),
          compile: jest.fn().mockReturnValue(mockValidator),
        };

        jest
          .spyOn(ajvModule, "getAjvInstance")
          .mockReturnValue(mockAjv as never);

        const credential = new TestMDLBuilder().build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "MobileSecurityObject does not comply with schema - /path: must be a string",
          );
        }
      });

      it("should should throw MDLValidationError and default path to 'root' when instancePath is missing", async () => {
        const mockValidator = jest
          .fn()
          .mockReturnValueOnce(true) // first call IssuerSigned (valid)
          .mockReturnValueOnce(false) as unknown as ValidateFunction; // second call MSO (invalid)
        mockValidator.errors = [
          {
            instancePath: "",
            message: "must be a string",
            data: 123,
            keyword: "key",
          } as unknown as ErrorObject,
        ];

        const mockAjv = {
          addSchema: jest.fn().mockReturnThis(),
          compile: jest.fn().mockReturnValue(mockValidator),
        };

        jest
          .spyOn(ajvModule, "getAjvInstance")
          .mockReturnValue(mockAjv as never);

        const credential = new TestMDLBuilder().build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "MobileSecurityObject does not comply with schema - root: must be a string",
          );
        }
      });

      it("should throw MDLValidationError and default to 'Unknown validation error' when message is missing", async () => {
        const mockValidator = jest
          .fn()
          .mockReturnValueOnce(true) // first call IssuerSigned (valid)
          .mockReturnValueOnce(false) as unknown as ValidateFunction; // second call MSO (invalid)
        mockValidator.errors = [
          {
            instancePath: "/path",
            message: undefined,
            data: 123,
            keyword: "key",
          } as unknown as ErrorObject,
        ];

        const mockAjv = {
          addSchema: jest.fn().mockReturnThis(),
          compile: jest.fn().mockReturnValue(mockValidator),
        };

        jest
          .spyOn(ajvModule, "getAjvInstance")
          .mockReturnValue(mockAjv as never);

        const credential = new TestMDLBuilder().build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "MobileSecurityObject does not comply with schema - /path: Unknown validation error",
          );
        }
      });

      it("should throw MDLValidationError with empty error details when validator.errors is undefined", async () => {
        const mockValidator = jest
          .fn()
          .mockReturnValueOnce(true) // first call IssuerSigned (valid)
          .mockReturnValueOnce(false) as unknown as ValidateFunction; // second call MSO (invalid)
        mockValidator.errors = undefined;

        const mockAjv = {
          addSchema: jest.fn().mockReturnThis(),
          compile: jest.fn().mockReturnValue(mockValidator),
        };

        jest
          .spyOn(ajvModule, "getAjvInstance")
          .mockReturnValue(mockAjv as never);

        const credential = new TestMDLBuilder().build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "MobileSecurityObject does not comply with schema - ",
          );
        }
      });
    });

    describe("Value digests", () => {
      it("should throw MDLValidationError when the payload's ValueDigests is missing a digest", async () => {
        const credential = new TestMDLBuilder()
          .withoutDigest("welsh_licence")
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "No digest found for digest ID 20 in MSO namespace org.iso.18013.5.1.GB: 30,40",
          );
        }
      });

      it("should throw MDLValidationError when digests don't match", async () => {
        const credential = new TestMDLBuilder()
          .withMismatchedDigest(
            "family_name",
            new Uint8Array(Buffer.from("incorrect-digest")),
          )
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "Digest mismatch for element identifier family_name with digest ID 10 in namespace org.iso.18013.5.1 - Expected 696e636f72726563742d646967657374 but calculated 40cb668b10272f8f5e6160d4e968d95d0c090f47c90306ebe934776ac076caba",
          );
        }
      });
    });

    describe("Device key", () => {
      it("should throw MDLValidationError when it has invalid keys", async () => {
        const credential = new TestMDLBuilder()
          .withDeviceKeyParameter(999, 1)
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "DeviceKey must contain exactly the keys [1, -1, -2, -3]",
          );
        }
      });

      it("should throw MDLValidationError when key type (1) is not EC2 (2)", async () => {
        const credential = new TestMDLBuilder()
          .withDeviceKeyParameter(1, 1)
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "DeviceKey key type (1) must be EC2 (Elliptic Curve) (2)",
          );
        }
      });

      it("should throw MDLValidationError when curve (-1) is not P-256 (1)", async () => {
        const credential = new TestMDLBuilder()
          .withDeviceKeyParameter(-1, 2)
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "DeviceKey curve (-1) must be P-256 (1)",
          );
        }
      });

      it("should throw MDLValidationError when x-coordinate (-2) is not a Uint8Array", async () => {
        const credential = new TestMDLBuilder()
          .withDeviceKeyParameter(-2, 123)
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "DeviceKey x-coordinate (-2) must be a Uint8Array",
          );
        }
      });

      it("should throw MDLValidationError when y-coordinate (-3) is not a Uint8Array", async () => {
        const credential = new TestMDLBuilder()
          .withDeviceKeyParameter(-3, "string")
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "DeviceKey y-coordinate (-3) must be a Uint8Array",
          );
        }
      });

      it("should throw MDLValidationError when it is not a valid public key", async () => {
        const credential = new TestMDLBuilder()
          .withDeviceKeyParameter(-2, new Uint8Array())
          .withDeviceKeyParameter(-3, new Uint8Array())
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe("Invalid elliptic curve key");
        }
      });
    });

    describe("Validity info", () => {
      it("should throw MDLValidationError when 'signed' is in the future", async () => {
        const credential = new TestMDLBuilder()
          .withValidityInfo({
            signed: new Tag(0, "2025-09-10T15:40:00Z"),
            validFrom: new Tag(0, "2025-09-10T15:40:00Z"),
          })
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "One or more dates are invalid - 'signed' (2025-09-10T15:40:00Z) must be in the past,'validFrom' (2025-09-10T15:40:00Z) must be in the past",
          );
        }
      });

      it("should throw MDLValidationError when 'validFrom' is before 'signed'", async () => {
        const credential = new TestMDLBuilder()
          .withValidityInfo({
            signed: new Tag(0, "2025-09-10T15:25:00Z"),
            validFrom: new Tag(0, "2025-09-10T15:20:00Z"),
          })
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "One or more dates are invalid - 'validFrom' (2026-09-10T15:20:00Z) must be equal or later than 'signed' (2025-09-10T15:25:00Z)",
          );
        }
      });

      it("should throw MDLValidationError when 'validUntil' is in the past", async () => {
        const credential = new TestMDLBuilder()
          .withValidityInfo({
            validUntil: new Tag(0, "2025-09-09T15:30:00Z"),
          })
          .build();

        expect.assertions(2);
        try {
          await isValidCredential(credential, rootCertificate);
        } catch (error) {
          expect(error).toBeInstanceOf(MDLValidationError);
          expect((error as Error).message).toBe(
            "One or more dates are invalid - 'validUntil' (2025-09-09T15:30:00Z) must be in the future",
          );
        }
      });
    });
  });

  it("should return true when credential is valid", async () => {
    const credential = new TestMDLBuilder().build();
    expect(await isValidCredential(credential, rootCertificate)).toBe(true);
  });
});
