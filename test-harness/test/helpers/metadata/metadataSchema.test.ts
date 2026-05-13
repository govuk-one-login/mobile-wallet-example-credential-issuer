import { getAjvInstance } from "../ajv/ajvInstance";
import { metadataSchema } from "./metadataSchema";

describe("metadataSchema", () => {
  const ajv = getAjvInstance();
  ajv.addSchema(
    { $id: "credential-configurations-supported", type: "object" },
    "credential-configurations-supported",
  );
  const validate = ajv.compile(metadataSchema);

  describe("Required Properties", () => {
    describe("credential_issuer", () => {
      it("should return false if it is missing", () => {
        const data = {
          authorization_servers: ["https://auth.example.com"],
          credential_endpoint: "https://cri.example.com/credential",
          credential_configurations_supported: {},
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "",
            message: "must have required property 'credential_issuer'",
          }),
        );
      });

      it("should return false if it is not a URI", () => {
        const data = {
          credential_issuer: "not-a-uri",
          authorization_servers: ["https://auth.example.com"],
          credential_endpoint: "https://cri.example.com/credential",
          credential_configurations_supported: {},
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/credential_issuer",
            message: 'must match format "uri"',
          }),
        );
      });
    });

    describe("authorization_servers", () => {
      it("should return false if it is missing", () => {
        const data = {
          credential_issuer: "https://cri.example.com",
          credential_endpoint: "https://cri.example.com/credential",
          credential_configurations_supported: {},
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "",
            message: "must have required property 'authorization_servers'",
          }),
        );
      });

      it("should return false if it is empty", () => {
        const data = {
          credential_issuer: "https://cri.example.com",
          authorization_servers: [],
          credential_endpoint: "https://cri.example.com/credential",
          credential_configurations_supported: {},
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/authorization_servers",
            message: "must NOT have fewer than 1 items",
          }),
        );
      });

      it("should return false if it contains non-URI values", () => {
        const data = {
          credential_issuer: "https://cri.example.com",
          authorization_servers: ["https://auth.example.com", "not-a-uri"],
          credential_endpoint: "https://cri.example.com/credential",
          credential_configurations_supported: {},
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/authorization_servers/1",
            message: 'must match format "uri"',
          }),
        );
      });
    });

    describe("credential_endpoint", () => {
      it("should return false if it is missing", () => {
        const data = {
          credential_issuer: "https://cri.example.com",
          authorization_servers: ["https://auth.example.com"],
          credential_configurations_supported: {},
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "",
            message: "must have required property 'credential_endpoint'",
          }),
        );
      });

      it("should return false if it is not a URI", () => {
        const data = {
          credential_issuer: "https://cri.example.com",
          authorization_servers: ["https://auth.example.com"],
          credential_endpoint: "not-a-uri",
          credential_configurations_supported: {},
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/credential_endpoint",
            message: 'must match format "uri"',
          }),
        );
      });
    });

    describe("credential_configurations_supported", () => {
      it("should return false if it is missing", () => {
        const data = {
          credential_issuer: "https://cri.example.com",
          authorization_servers: ["https://auth.example.com"],
          credential_endpoint: "https://cri.example.com/credential",
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "",
            message:
              "must have required property 'credential_configurations_supported'",
          }),
        );
      });
    });
  });

  describe("Optional Properties", () => {
    describe("notification_endpoint", () => {
      it("should return false if it is not a URI", () => {
        const data = {
          credential_issuer: "https://cri.example.com",
          authorization_servers: ["https://auth.example.com"],
          credential_endpoint: "https://cri.example.com/credential",
          credential_configurations_supported: {},
          notification_endpoint: "not-a-uri",
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/notification_endpoint",
            message: 'must match format "uri"',
          }),
        );
      });
    });

    describe("mdoc_iacas_uri", () => {
      it("should return false if it is not a URI", () => {
        const data = {
          credential_issuer: "https://cri.example.com",
          authorization_servers: ["https://auth.example.com"],
          credential_endpoint: "https://cri.example.com/credential",
          credential_configurations_supported: {},
          mdoc_iacas_uri: "not-a-uri",
        };
        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/mdoc_iacas_uri",
            message: 'must match format "uri"',
          }),
        );
      });
    });
  });

  describe("Additional Properties", () => {
    it("should return true when there are additional properties", () => {
      const data = {
        credential_issuer: "https://cri.example.com",
        authorization_servers: ["https://auth.example.com"],
        credential_endpoint: "https://cri.example.com/credential",
        credential_configurations_supported: {},
        extra_property: "allowed",
      };

      const isValid = validate(data);

      expect(isValid).toBe(true);
    });
  });

  describe("Valid Configuration", () => {
    it("should return true when only required parameters are present", () => {
      const data = {
        credential_issuer: "https://cri.example.com",
        authorization_servers: ["https://auth.example.com"],
        credential_endpoint: "https://cri.example.com/credential",
        credential_configurations_supported: {},
      };

      const isValid = validate(data);

      expect(isValid).toBe(true);
    });

    it("should return true when required and optional parameters are present", () => {
      const data = {
        credential_issuer: "https://cri.example.com",
        authorization_servers: ["https://auth.example.com"],
        credential_endpoint: "https://cri.example.com/credential",
        credential_configurations_supported: {},
        notification_endpoint: "https://cri.example.com/notification",
        mdoc_iacas_uri: "https://cri.example.com/iacas",
      };

      const isValid = validate(data);

      expect(isValid).toBe(true);
    });
  });
});
