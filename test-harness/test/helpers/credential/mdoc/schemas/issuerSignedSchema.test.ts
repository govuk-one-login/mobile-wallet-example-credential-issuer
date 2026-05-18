import { getAjvInstance } from "../../../ajv/ajvInstance";
import { issuerSignedSchema } from "./issuerSignedSchema";

describe("issuerSignedSchema", () => {
  const ajv = getAjvInstance();
  if (!ajv.getSchema("iso-namespace")) {
    ajv.addSchema({ $id: "iso-namespace", type: "array" });
  }
  if (!ajv.getSchema("domestic-namespace")) {
    ajv.addSchema({ $id: "domestic-namespace", type: "array" });
  }
  const validate = ajv.compile(issuerSignedSchema);

  it("should return false when it contains additional properties", () => {
    const data = {
      nameSpaces: {
        "org.iso.18013.5.1": [],
        "org.iso.18013.5.1.GB": [],
      },
      issuerAuth: [
        new Uint8Array(),
        new Map(),
        new Uint8Array(),
        new Uint8Array(),
      ],
      extraProperty: "not allowed",
    };

    const isValid = validate(data);

    expect(isValid).toBe(false);
    expect(validate.errors).toContainEqual(
      expect.objectContaining({
        instancePath: "",
        message: "must NOT have additional properties",
      }),
    );
  });

  describe("issuerAuth", () => {
    it("should return false when it contains additional items", () => {
      const data = {
        nameSpaces: {
          "org.iso.18013.5.1": [],
          "org.iso.18013.5.1.GB": [],
        },
        issuerAuth: [
          new Uint8Array(),
          new Map(),
          new Uint8Array(),
          new Uint8Array(),
          new Uint8Array(), // additional item
        ],
      };

      const isValid = validate(data);

      expect(isValid).toBe(false);
      console.log(validate.errors);
      expect(validate.errors).toContainEqual(
        expect.objectContaining({
          instancePath: "/issuerAuth",
          message: "must NOT have more than 4 items",
        }),
      );
    });
  });

  it("should return true when data is valid", () => {
    const data = {
      nameSpaces: {
        "org.iso.18013.5.1": [],
        "org.iso.18013.5.1.GB": [],
      },
      issuerAuth: [
        new Uint8Array(),
        new Map(),
        new Uint8Array(),
        new Uint8Array(),
      ],
    };

    const isValid = validate(data);

    expect(isValid).toBe(true);
  });
});
