import { getAjvInstance } from "../../../ajv/ajvInstance";
import { mobileSecurityObjectSchema } from "./mobileSecurityObjectSchema";
import { testMobileSecurityObjectBuilder } from "./testMobileSecurityObjectBuilder";
import { MobileSecurityObject } from "../types/mobileSecurityObject";

describe("mobileSecurityObjectSchema", () => {
  const ajv = getAjvInstance();
  const validate = ajv.compile(mobileSecurityObjectSchema);

  describe("deviceKeyInfo", () => {
    it("should return false when it contains additional properties", () => {
      const dataWithAdditionalItems = testMobileSecurityObjectBuilder(
        defaultData,
      ).withOverrides({
        extraProperty: "not allowed",
      });

      const isValid = validate(dataWithAdditionalItems);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "",
          params: { additionalProperty: "extraProperty" },
          message: "must NOT have additional properties",
        }),
      );
    });
  });

  describe("version", () => {
    it("should return false when it is not '1.0'", () => {
      const data = testMobileSecurityObjectBuilder(defaultData).withOverrides({
        version: "2.0",
      });

      const isValid = validate(data);

      expect(isValid).toBe(false);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/version",
          params: { allowedValues: ["1.0"] },
          message: "must be equal to one of the allowed values",
        }),
      );
    });
  });

  it("should return true when data is valid", () => {
    const isValid = validate(defaultData);

    expect(isValid).toBe(true);
  });
});

const defaultData: MobileSecurityObject = {
  version: "1.0",
  digestAlgorithm: "SHA-256",
  deviceKeyInfo: {
    deviceKey: new Map(),
    keyAuthorizations: {
      nameSpaces: ["org.iso.18013.5.1", "org.iso.18013.5.1.GB"],
    },
  },
  valueDigests: {
    "org.iso.18013.5.1": new Map(),
    "org.iso.18013.5.1.GB": new Map(),
  },
  docType: "org.iso.18013.5.1.mDL",
  validityInfo: {
    signed: "2023-10-10T10:10:10Z",
    validFrom: "2023-10-10T10:10:10Z",
    validUntil: "2024-10-10T10:10:10Z",
  },
  status: {
    status_list: {
      idx: 1,
      uri: "https://example.com/status",
    },
  },
};
