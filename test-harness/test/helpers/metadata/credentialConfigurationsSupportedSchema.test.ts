import { getAjvInstance } from "../ajv/ajvInstance";
import { credentialConfigurationsSupportedSchema } from "./credentialConfigurationsSupportedSchema";

describe("credentialConfigurationsSupportedSchema", () => {
  const ajv = getAjvInstance();
  const validate = ajv.compile(credentialConfigurationsSupportedSchema);

  describe("Required Properties", () => {
    describe("format", () => {
      it("should return false if it is missing", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/org.iso.18013.5.1.mDL",
            message: "must have required property 'format'",
          }),
        );
      });

      it("should return false if it is neither 'jwt_vc_json' nor 'mso_mdoc'", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "unknown_format",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/org.iso.18013.5.1.mDL/format",
            params: { allowedValues: ["jwt_vc_json", "mso_mdoc"] },
            message: "must be equal to one of the allowed values",
          }),
        );
      });
    });

    describe("cryptographic_binding_methods_supported", () => {
      it("should return false if it is missing", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: undefined,
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/org.iso.18013.5.1.mDL",
            message:
              "must have required property 'cryptographic_binding_methods_supported'",
          }),
        );
      });

      it("should return false if it is empty", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: [],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/org.iso.18013.5.1.mDL/cryptographic_binding_methods_supported",
            message: "must NOT have fewer than 1 items",
          }),
        );
      });

      it("should return true if it contains more than one item", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key", "did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(true);
      });
    });

    describe("credential_signing_alg_values_supported", () => {
      it("should return false if it is missing", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            credential_signing_alg_values_supported: undefined,
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/org.iso.18013.5.1.mDL",
            message:
              "must have required property 'credential_signing_alg_values_supported'",
          }),
        );
      });

      it("should return false if it is empty", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: [],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/org.iso.18013.5.1.mDL/credential_signing_alg_values_supported",
            message: "must NOT have fewer than 1 items",
          }),
        );
      });

      it("should return false if it does not contain 'ES256'", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["RS256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/org.iso.18013.5.1.mDL/credential_signing_alg_values_supported",
            message: "must contain at least 1 valid item(s)",
          }),
        );
      });

      it("should return true if it contains more than one item", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256", "extra"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(true);
      });
    });

    describe("credential_validity_period_max_days", () => {
      it("should return false if it is missing", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/org.iso.18013.5.1.mDL",
            message:
              "must have required property 'credential_validity_period_max_days'",
          }),
        );
      });

      it("should return false if it is not a number", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: "30",
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/org.iso.18013.5.1.mDL/credential_validity_period_max_days",
            message: "must be number",
          }),
        );
      });
    });

    describe("credential_refresh_web_journey_url", () => {
      it("should return false if it is not a valid URI", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "not-a-url",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/org.iso.18013.5.1.mDL/credential_refresh_web_journey_url",
            message: 'must match format "uri"',
          }),
        );
      });

      it("should return true if it is a valid URI", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(true);
      });

      it("should return false if it is missing", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/org.iso.18013.5.1.mDL",
            message:
              "must have required property 'credential_refresh_web_journey_url'",
          }),
        );
      });
    });
  });

  describe("Additional Properties", () => {
    it("should return true if there are additional properties", () => {
      const data = {
        "org.iso.18013.5.1.mDL": {
          format: "mso_mdoc",
          doctype: "org.iso.18013.5.1.mDL",
          cryptographic_binding_methods_supported: ["cose_key"],
          credential_signing_alg_values_supported: ["ES256"],
          credential_validity_period_max_days: 30,
          credential_refresh_web_journey_url: "https://example.com/refresh",
          extra_property: "allowed",
        },
      };

      const isValid = validate(data);

      expect(isValid).toBe(true);
    });
  });

  describe("JWT ('jwt_vc_json') format", () => {
    describe("proof_types_supported", () => {
      it("should return false if it is missing", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["VerifiableCredential", "SocialSecurityCredential"],
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/SocialSecurityCredential",
            message: "must have required property 'proof_types_supported'",
          }),
        );
      });

      it("should return false if it is missing 'jwt'", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["VerifiableCredential", "SocialSecurityCredential"],
            },
            proof_types_supported: {},
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/SocialSecurityCredential/proof_types_supported",
            message: "must have required property 'jwt'",
          }),
        );
      });

      it("should return false if 'jwt' is missing 'proof_signing_alg_values_supported'", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["VerifiableCredential", "SocialSecurityCredential"],
            },
            proof_types_supported: {
              jwt: {},
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/SocialSecurityCredential/proof_types_supported/jwt",
            message:
              "must have required property 'proof_signing_alg_values_supported'",
          }),
        );
      });

      it("should return false if 'jwt.proof_signing_alg_values_supported' is empty", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["VerifiableCredential", "SocialSecurityCredential"],
            },
            proof_types_supported: {
              jwt: {
                proof_signing_alg_values_supported: [],
              },
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/SocialSecurityCredential/proof_types_supported/jwt/proof_signing_alg_values_supported",
            message: "must NOT have fewer than 1 items",
          }),
        );
      });

      it("should return false if 'jwt.proof_signing_alg_values_supported' does not contain 'ES256'", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["VerifiableCredential", "SocialSecurityCredential"],
            },
            proof_types_supported: {
              jwt: {
                proof_signing_alg_values_supported: ["RS256", "ES384"],
              },
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/SocialSecurityCredential/proof_types_supported/jwt/proof_signing_alg_values_supported",
            message: "must contain at least 1 valid item(s)",
          }),
        );
      });
    });

    describe("credential_definition", () => {
      it("should return false if it is missing", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            proof_types_supported: {
              jwt: {
                proof_signing_alg_values_supported: ["ES256"],
              },
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/SocialSecurityCredential",
            message: "must have required property 'credential_definition'",
          }),
        );
      });

      it("should return false if 'type' has less than two items", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["VerifiableCredential"],
            },
            proof_types_supported: {
              jwt: {
                proof_signing_alg_values_supported: ["ES256"],
              },
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/SocialSecurityCredential/credential_definition/type",
            message: "must NOT have fewer than 2 items",
          }),
        );
      });

      it("should return false if 'type' has more than two items", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: [
                "VerifiableCredential",
                "SocialSecurityCredential",
                "Extra",
              ],
            },
            proof_types_supported: {
              jwt: {
                proof_signing_alg_values_supported: ["ES256"],
              },
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/SocialSecurityCredential/credential_definition/type",
            message: "must NOT have more than 2 items",
          }),
        );
      });

      it("should return false if 'type' is missing 'VerifiableCredential'", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["SomethingElse", "SocialSecurityCredential"],
            },
            proof_types_supported: {
              jwt: {
                proof_signing_alg_values_supported: ["ES256"],
              },
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/SocialSecurityCredential/credential_definition/type",
            message: "must contain at least 1 valid item(s)",
          }),
        );
      });

      it("should return false if 'type' contains non-string values", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["VerifiableCredential", 123],
            },
            proof_types_supported: {
              jwt: {
                proof_signing_alg_values_supported: ["ES256"],
              },
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/SocialSecurityCredential/credential_definition/type/1",
            message: "must be string",
          }),
        );
      });

      it("should return true if there are additional properties", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["VerifiableCredential", "SocialSecurityCredential"],
              extra_property: "allowed",
            },
            proof_types_supported: {
              extra_property: "allowed",
              jwt: {
                proof_signing_alg_values_supported: ["ES256"],
                extra_property: "allowed",
              },
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(true);
      });
    });

    describe("cryptographic_binding_methods_supported", () => {
      it("should return false if it does not contain 'did:key'", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["VerifiableCredential", "SocialSecurityCredential"],
            },
            cryptographic_binding_methods_supported: ["something_else"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/SocialSecurityCredential/cryptographic_binding_methods_supported/0",
            params: { allowedValue: "did:key" },
            message: "must be equal to constant",
          }),
        );
      });
    });

    describe("Valid Configuration", () => {
      it("should return true", () => {
        const data = {
          SocialSecurityCredential: {
            format: "jwt_vc_json",
            credential_definition: {
              type: ["VerifiableCredential", "SocialSecurityCredential"],
            },
            proof_types_supported: {
              jwt: {
                proof_signing_alg_values_supported: ["ES256"],
              },
            },
            cryptographic_binding_methods_supported: ["did:key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(true);
      });
    });
  });

  describe("mDoc ('mso_mdoc') format", () => {
    describe("doctype", () => {
      it("should return false if it is missing", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/org.iso.18013.5.1.mDL",
            message: "must have required property 'doctype'",
          }),
        );
      });

      it("should return false if it is not a string", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: 1,
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath: "/org.iso.18013.5.1.mDL/doctype",
            message: "must be string",
          }),
        );
      });
    });

    describe("cryptographic_binding_methods_supported", () => {
      it("should return false if it does not contain 'cose_key'", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["something_else"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };
        const isValid = validate(data);

        expect(isValid).toBe(false);
        expect(validate.errors).toContainEqual(
          expect.objectContaining({
            instancePath:
              "/org.iso.18013.5.1.mDL/cryptographic_binding_methods_supported/0",
            params: { allowedValue: "cose_key" },
            message: "must be equal to constant",
          }),
        );
      });
    });

    describe("Valid Configuration", () => {
      it("should return true", () => {
        const data = {
          "org.iso.18013.5.1.mDL": {
            format: "mso_mdoc",
            doctype: "org.iso.18013.5.1.mDL",
            cryptographic_binding_methods_supported: ["cose_key"],
            credential_signing_alg_values_supported: ["ES256"],
            credential_validity_period_max_days: 30,
            credential_refresh_web_journey_url: "https://example.com/refresh",
          },
        };

        const isValid = validate(data);

        expect(isValid).toBe(true);
      });
    });
  });

  describe("Multiple Credentials", () => {
    it("should return false when any are invalid", () => {
      const data = {
        InvalidCredential: {
          format: "invalid_format",
          doctype: "org.iso.18013.5.1.mDL",
          cryptographic_binding_methods_supported: ["cose_key"],
          credential_signing_alg_values_supported: ["ES256"],
          credential_validity_period_max_days: 30,
          credential_refresh_web_journey_url: "https://example.com/refresh",
        },
        SocialSecurityCredential: {
          format: "jwt_vc_json",
          credential_definition: {
            type: ["VerifiableCredential", "SocialSecurityCredential"],
          },
          proof_types_supported: {
            jwt: {
              proof_signing_alg_values_supported: ["ES256"],
            },
          },
          cryptographic_binding_methods_supported: ["did:key"],
          credential_signing_alg_values_supported: ["ES256"],
          credential_validity_period_max_days: 30,
          credential_refresh_web_journey_url: "https://example.com/refresh",
        },
      };

      const isValid = validate(data);

      expect(isValid).toBe(false);
    });

    it("should return true when all are valid", () => {
      const data = {
        "org.iso.18013.5.1.mDL": {
          format: "mso_mdoc",
          doctype: "org.iso.18013.5.1.mDL",
          cryptographic_binding_methods_supported: ["cose_key"],
          credential_signing_alg_values_supported: ["ES256"],
          credential_validity_period_max_days: 30,
          credential_refresh_web_journey_url: "https://example.com/refresh",
        },
        SocialSecurityCredential: {
          format: "jwt_vc_json",
          credential_definition: {
            type: ["VerifiableCredential", "SocialSecurityCredential"],
          },
          proof_types_supported: {
            jwt: {
              proof_signing_alg_values_supported: ["ES256"],
            },
          },
          cryptographic_binding_methods_supported: ["did:key"],
          credential_signing_alg_values_supported: ["ES256"],
          credential_validity_period_max_days: 30,
          credential_refresh_web_journey_url: "https://example.com/refresh",
        },
      };

      const isValid = validate(data);

      expect(isValid).toBe(true);
    });
  });
});
